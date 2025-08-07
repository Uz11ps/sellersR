package org.example.controller;

import org.example.entity.Subscription;
import org.example.entity.User;
import org.example.repository.SubscriptionRepository;
import org.example.repository.UserRepository;
import org.example.service.AuthService;
import org.example.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {
    
    // Кэш для отслеживания обработанных запросов (предотвращение дублирования)
    private static final ConcurrentHashMap<String, Boolean> processedRequests = new ConcurrentHashMap<>();
    
    // Максимальное количество запросов в кэше
    private static final int MAX_CACHE_SIZE = 1000;
    
    // Время жизни записи в кэше (в миллисекундах)
    private static final long CACHE_TTL = 3600000; // 1 час
    
    @Autowired
    private SubscriptionService subscriptionService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private AuthService authService;
    
    /**
     * Получение информации о доступных планах подписки
     */
    @GetMapping("/plans")
    public ResponseEntity<?> getSubscriptionPlans() {
        try {
            System.out.println("📋 Получен запрос на получение планов подписки");
            
            // Здесь можно получать планы из базы данных, но для простоты используем статические данные
            List<Map<String, Object>> plans = List.of(
                Map.of(
                    "planType", "PLAN_FREE",
                    "displayName", "Бесплатный тестовый",
                    "price", 0.0,
                    "days", 7, // дней
                    "features", List.of(
                        "Базовая аналитика",
                        "Тестовый доступ",
                        "7 дней"
                    )
                ),
                Map.of(
                    "planType", "PLAN_30_DAYS",
                    "displayName", "30 дней",
                    "price", 1499.0,
                    "days", 30, // дней
                    "features", List.of(
                        "Финансовая таблица",
                        "ABC-анализ",
                        "Планирование поставок"
                    )
                ),
                Map.of(
                    "planType", "PLAN_60_DAYS",
                    "displayName", "60 дней",
                    "price", 2799.0,
                    "days", 60, // дней
                    "features", List.of(
                        "Финансовая таблица",
                        "ABC-анализ",
                        "Планирование поставок",
                        "Отслеживание промоакций",
                        "Приоритетная поддержка"
                    )
                ),
                Map.of(
                    "planType", "PLAN_90_DAYS",
                    "displayName", "90 дней",
                    "price", 3999.0,
                    "days", 90, // дней
                    "features", List.of(
                        "Все функции 60-дневного плана",
                        "Расширенная аналитика",
                        "Интеграция с 1С",
                        "Персональный менеджер",
                        "API доступ"
                    )
                )
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "plans", plans
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения планов подписки: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка получения планов подписки: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Получение информации о текущей подписке пользователя
     */
    @GetMapping("/info")
    public ResponseEntity<?> getSubscriptionInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        try {
            System.out.println("🔍 Получен запрос на получение информации о подписке");
            
            String userEmail = null;
            boolean tokenExpired = false;
            
            // Проверяем, есть ли информация о истекшем токене в атрибутах запроса
            if (request.getAttribute("expiredToken") != null && (Boolean)request.getAttribute("expiredToken")) {
                userEmail = (String) request.getAttribute("expiredTokenEmail");
                tokenExpired = true;
                System.out.println("⏰ Запрос с истекшим токеном для пользователя: " + userEmail);
            } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // Стандартная обработка токена
                String token = authHeader.substring(7);
                userEmail = authService.extractEmailFromToken(token);
            }
            
            // Если не удалось получить email, возвращаем ошибку
            if (userEmail == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Требуется авторизация",
                    "tokenExpired", tokenExpired
                ));
            }
            
            User user = userRepository.findByEmail(userEmail)
                    .orElse(null);
            
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Пользователь не найден",
                    "tokenExpired", tokenExpired
                ));
            }
            
            // Получаем активную подписку пользователя
            Subscription subscription = subscriptionService.getActiveSubscription(user);
            
            if (subscription == null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "hasSubscription", false,
                    "tokenExpired", tokenExpired
                ));
            }
            
            Map<String, Object> subscriptionData = new HashMap<>();
            subscriptionData.put("id", subscription.getId());
            subscriptionData.put("planId", subscription.getPlanId());
            subscriptionData.put("planType", subscription.getPlanType().name());
            subscriptionData.put("status", subscription.getStatus().name());
            subscriptionData.put("startDate", subscription.getStartDate().toString());
            subscriptionData.put("endDate", subscription.getEndDate().toString());
            subscriptionData.put("active", subscription.isActive());
            subscriptionData.put("autoRenew", subscription.isAutoRenew());
            subscriptionData.put("trial", subscription.getPlanType() == Subscription.PlanType.PLAN_FREE);
            
            // Добавляем информацию о плане
            String planType = subscription.getPlanType().name();
            Map<String, Object> planInfo = switch (planType) {
                case "PLAN_FREE" -> Map.of(
                    "name", "Бесплатный тестовый",
                    "price", 0.0
                );
                case "PLAN_30_DAYS" -> Map.of(
                    "name", "30 дней",
                    "price", 1499.0
                );
                case "PLAN_60_DAYS" -> Map.of(
                    "name", "60 дней",
                    "price", 2799.0
                );
                case "PLAN_90_DAYS" -> Map.of(
                    "name", "90 дней",
                    "price", 3999.0
                );
                default -> Map.of(
                    "name", "Неизвестный план",
                    "price", 0.0
                );
            };
            
            subscriptionData.put("plan", planInfo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasSubscription", true);
            response.put("subscription", subscriptionData);
            response.put("tokenExpired", tokenExpired);
            
            if (tokenExpired) {
                // Генерируем новый токен для пользователя
                String newToken = authService.generateNewToken(user);
                response.put("newToken", newToken);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения информации о подписке: " + e.getMessage());
            e.printStackTrace(); // Добавляем стек-трейс для отладки
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка получения информации о подписке: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Создание новой подписки
     */
    @PostMapping("/create")
    public ResponseEntity<?> createSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            System.out.println("📝 Получен запрос на создание подписки");
            System.out.println("Request body: " + request);
            
            String userEmail = null;
            boolean tokenExpired = false;
            
            // Проверяем, есть ли информация о истекшем токене в атрибутах запроса
            if (httpRequest.getAttribute("expiredToken") != null && (Boolean)httpRequest.getAttribute("expiredToken")) {
                userEmail = (String) httpRequest.getAttribute("expiredTokenEmail");
                tokenExpired = true;
                System.out.println("⏰ Запрос с истекшим токеном для пользователя: " + userEmail);
            } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // Стандартная обработка токена
                String token = authHeader.substring(7);
                userEmail = authService.extractEmailFromToken(token);
            }
            
            if (userEmail == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Требуется авторизация",
                    "tokenExpired", tokenExpired
                ));
            }
            
            User user = userRepository.findByEmail(userEmail)
                    .orElse(null);
            
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Пользователь не найден",
                    "tokenExpired", tokenExpired
                ));
            }
            
            // Получаем planId из запроса
            String planId = request.get("planId");
            System.out.println("Received planId: " + planId);
            
            if (planId == null || planId.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Необходимо указать план подписки",
                    "tokenExpired", tokenExpired
                ));
            }
            
            // Проверяем валидность planId
            if (!planId.equals("free") && !planId.equals("basic") && !planId.equals("pro") && !planId.equals("enterprise")) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Неверный идентификатор плана подписки: " + planId,
                    "tokenExpired", tokenExpired
                ));
            }
            
            // Проверяем, есть ли у пользователя активная подписка
            if (subscriptionService.hasActiveSubscription(user)) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "У пользователя уже есть активная подписка",
                    "tokenExpired", tokenExpired
                ));
            }
            
            // Создаем новую подписку
            Subscription subscription = new Subscription();
            subscription.setUser(user);
            subscription.setPlanId(planId);
            subscription.setStartDate(LocalDateTime.now());
            subscription.setTrialPeriod(planId.equals("free"));
            
            // Устанавливаем срок действия подписки в зависимости от плана
            int durationDays = switch (planId) {
                case "free" -> 7; // 7 дней для бесплатного плана
                case "basic", "pro", "enterprise" -> 30;
                default -> 30;
            };
            
            subscription.setEndDate(LocalDateTime.now().plusDays(durationDays));
            subscription.setActive(true);
            
            // Сохраняем подписку
            subscription = subscriptionRepository.save(subscription);
            
            System.out.println("✅ Подписка создана успешно: " + subscription.getId());
            
            // Формируем ответ с информацией о подписке
            Map<String, Object> subscriptionData = new HashMap<>();
            subscriptionData.put("id", subscription.getId());
            subscriptionData.put("planId", subscription.getPlanId());
            subscriptionData.put("startDate", subscription.getStartDate().toString());
            subscriptionData.put("endDate", subscription.getEndDate().toString());
            subscriptionData.put("active", subscription.isActive());
            subscriptionData.put("trial", subscription.isTrialPeriod());
            
            // Добавляем информацию о плане
            Map<String, Object> planInfo = switch (planId) {
                case "free" -> Map.of(
                    "name", "Бесплатный тестовый",
                    "price", 0
                );
                case "basic" -> Map.of(
                    "name", "Базовый",
                    "price", 990
                );
                case "pro" -> Map.of(
                    "name", "Профессиональный",
                    "price", 1990
                );
                case "enterprise" -> Map.of(
                    "name", "Корпоративный",
                    "price", 4990
                );
                default -> Map.of(
                    "name", "Неизвестный план",
                    "price", 0
                );
            };
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Подписка успешно создана");
            response.put("subscription", subscriptionData);
            response.put("plan", planInfo);
            
            if (tokenExpired) {
                response.put("tokenExpired", true);
                // Генерируем новый токен для пользователя
                String newToken = authService.generateNewToken(user);
                response.put("newToken", newToken);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка создания подписки: " + e.getMessage());
            e.printStackTrace(); // Добавляем вывод стека ошибки для отладки
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка создания подписки: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Отмена подписки
     */
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        try {
            System.out.println("❌ Получен запрос на отмену подписки");
            
            String userEmail = null;
            boolean tokenExpired = false;
            
            // Проверяем, есть ли информация о истекшем токене в атрибутах запроса
            if (request.getAttribute("expiredToken") != null && (Boolean)request.getAttribute("expiredToken")) {
                userEmail = (String) request.getAttribute("expiredTokenEmail");
                tokenExpired = true;
                System.out.println("⏰ Запрос с истекшим токеном для пользователя: " + userEmail);
            } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // Стандартная обработка токена
                String token = authHeader.substring(7);
                userEmail = authService.extractEmailFromToken(token);
            }
            
            if (userEmail == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Требуется авторизация",
                    "tokenExpired", tokenExpired
                ));
            }
            
            User user = userRepository.findByEmail(userEmail)
                    .orElse(null);
            
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Пользователь не найден",
                    "tokenExpired", tokenExpired
                ));
            }
            
            // Получаем активную подписку пользователя
            Subscription subscription = subscriptionService.getActiveSubscription(user);
            
            if (subscription == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "У пользователя нет активной подписки",
                    "tokenExpired", tokenExpired
                ));
            }
            
            // Отменяем подписку
            subscription.setActive(false);
            subscriptionRepository.save(subscription);
            
            System.out.println("✅ Подписка отменена успешно: " + subscription.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Подписка успешно отменена");
            
            if (tokenExpired) {
                response.put("tokenExpired", true);
                // Генерируем новый токен для пользователя
                String newToken = authService.generateNewToken(user);
                response.put("newToken", newToken);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка отмены подписки: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка отмены подписки: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Для тестирования: создание бесплатной пробной подписки
     */
    @PostMapping("/create-trial")
    public ResponseEntity<?> createTrialSubscription(
            @RequestBody(required = false) Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            System.out.println("\n\n🎁 ===== НАЧАЛО СОЗДАНИЯ ПРОБНОЙ ПОДПИСКИ =====");
            System.out.println("📦 Request body: " + (requestBody != null ? requestBody : "EMPTY"));
            
            String userEmail = null;
            
            // Пытаемся получить email из тела запроса
            if (requestBody != null && requestBody.containsKey("email")) {
                userEmail = requestBody.get("email");
                System.out.println("📧 Email из тела запроса: " + userEmail);
            }
            
            System.out.println("📧 Итоговый email для создания подписки: " + userEmail);
            
            // Если email все еще не найден, возвращаем ошибку
            if (userEmail == null) {
                System.out.println("❌ Email не найден в запросе");
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Требуется указать email пользователя",
                    "error", "MissingEmail",
                    "path", request.getRequestURI()
                ));
            }
            
            // Ищем пользователя по email
            System.out.println("🔍 Поиск пользователя по email: " + userEmail);
            User user = userRepository.findByEmail(userEmail)
                    .orElse(null);
            
            // Если пользователь не найден, возвращаем ошибку
            if (user == null) {
                System.out.println("❌ Пользователь не найден: " + userEmail);
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Пользователь не найден",
                    "error", "UserNotFound",
                    "path", request.getRequestURI()
                ));
            }
            
            System.out.println("✅ Пользователь найден: " + user.getEmail() + " (ID: " + user.getId() + ")");
            
            // Проверяем, есть ли у пользователя активная подписка
            System.out.println("🔍 Проверка наличия активной подписки...");
            boolean hasActiveSubscription = subscriptionService.hasActiveSubscription(user);
            System.out.println("📊 Результат проверки: " + (hasActiveSubscription ? "Есть активная подписка" : "Нет активной подписки"));
            
            if (hasActiveSubscription) {
                System.out.println("⚠️ У пользователя уже есть активная подписка: " + userEmail);
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "У пользователя уже есть активная подписка",
                    "error", "SubscriptionExists",
                    "path", request.getRequestURI()
                ));
            }
            
            // Создаем новую пробную подписку
            System.out.println("📝 Создание новой пробной подписки...");
            Subscription subscription = new Subscription();
            subscription.setUser(user);
            subscription.setPlanType(Subscription.PlanType.PLAN_FREE); // Бесплатная подписка
            subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
            subscription.setStartDate(LocalDateTime.now());
            subscription.setEndDate(LocalDateTime.now().plusDays(7)); // 7 дней пробного периода
            subscription.setPrice(0.0); // Бесплатно
            subscription.setAutoRenew(false);
            subscription.setPaymentMethod("FREE_TRIAL");
            
            // Сохраняем подписку
            try {
                System.out.println("💾 Сохранение подписки в базу данных...");
                subscription = subscriptionRepository.save(subscription);
                System.out.println("✅ Пробная подписка создана успешно: " + subscription.getId() + " для пользователя: " + userEmail);
            } catch (Exception e) {
                System.err.println("❌ Ошибка при сохранении подписки: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Ошибка при сохранении подписки: " + e.getMessage(),
                    "error", "DatabaseError",
                    "path", request.getRequestURI()
                ));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Пробная подписка успешно создана");
            response.put("subscription", Map.of(
                "id", subscription.getId(),
                "planId", subscription.getPlanId(),
                "startDate", subscription.getStartDate().toString(),
                "endDate", subscription.getEndDate().toString(),
                "active", subscription.isActive(),
                "trial", true
            ));
            
            // Токен не используется для бесплатной подписки
            
            System.out.println("🎁 ===== ЗАВЕРШЕНИЕ СОЗДАНИЯ ПРОБНОЙ ПОДПИСКИ =====\n\n");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Критическая ошибка создания пробной подписки: " + e.getMessage());
            e.printStackTrace(); // Добавляем стек-трейс для отладки
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка создания пробной подписки: " + e.getMessage(),
                "error", e.getClass().getSimpleName(),
                "path", request.getRequestURI()
            ));
        }
    }

    /**
     * Отладочный метод для проверки всех подписок пользователя
     */
    @GetMapping("/debug/user-subscriptions")
    public ResponseEntity<?> debugUserSubscriptions(
            @RequestParam String email) {
        try {
            System.out.println("🔍 Отладка: проверка подписок пользователя: " + email);
            
            // Ищем пользователя по email
            User user = userRepository.findByEmail(email)
                    .orElse(null);
            
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Пользователь не найден"
                ));
            }
            
            // Получаем все подписки пользователя
            List<Subscription> allSubscriptions = subscriptionRepository.findByUser(user);
            
            // Получаем активные подписки пользователя
            List<Subscription> activeSubscriptions = subscriptionRepository.findByUserAndStatus(
                user, Subscription.SubscriptionStatus.ACTIVE);
            
            // Проверяем, есть ли активная подписка через сервис
            boolean hasActiveSubscription = subscriptionService.hasActiveSubscription(user);
            
            // Формируем ответ с информацией о подписках
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("hasActiveSubscription", hasActiveSubscription);
            response.put("totalSubscriptions", allSubscriptions.size());
            response.put("activeSubscriptionsCount", activeSubscriptions.size());
            
            // Преобразуем подписки в формат для отображения
            List<Map<String, Object>> subscriptionsList = allSubscriptions.stream()
                .map(subscription -> {
                    Map<String, Object> subscriptionData = new HashMap<>();
                    subscriptionData.put("id", subscription.getId());
                    subscriptionData.put("planType", subscription.getPlanType() != null ? subscription.getPlanType().name() : "null");
                    subscriptionData.put("status", subscription.getStatus() != null ? subscription.getStatus().name() : "null");
                    subscriptionData.put("startDate", subscription.getStartDate() != null ? subscription.getStartDate().toString() : "null");
                    subscriptionData.put("endDate", subscription.getEndDate() != null ? subscription.getEndDate().toString() : "null");
                    subscriptionData.put("isActive", subscription.isActive());
                    subscriptionData.put("price", subscription.getPrice());
                    subscriptionData.put("createdAt", subscription.getCreatedAt() != null ? subscription.getCreatedAt().toString() : "null");
                    return subscriptionData;
                })
                .collect(Collectors.toList());
            
            response.put("subscriptions", subscriptionsList);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка при отладке подписок: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка при отладке подписок: " + e.getMessage()
            ));
        }
    }
} 