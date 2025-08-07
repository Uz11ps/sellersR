package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.auth.*;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.AuthService;
import org.example.service.JwtService;
import org.example.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SubscriptionService subscriptionService;
    
    @Autowired
    private JwtService jwtService;
    
    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            System.out.println("📝 Получен запрос на регистрацию: " + request.getEmail());
            AuthResponse response = authService.register(request);
            String verificationCode = authService.getVerificationCode(request.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Регистрация успешна",
                "token", response.getToken(),
                "user", response,
                "verificationCode", verificationCode,
                "telegramBot", "@SellersWilberis_bot"
            ));
        } catch (Exception e) {
            System.err.println("❌ Ошибка регистрации: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            System.out.println("🔑 Получен запрос на авторизацию: " + request.getEmail());
            AuthResponse response = authService.authenticate(request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Авторизация успешна",
                "token", response.getToken(),
                "user", response
            ));
        } catch (Exception e) {
            System.err.println("❌ Ошибка авторизации: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request) {
        try {
            String verificationCode = request.get("verificationCode");
            
            System.out.println("🔍 Получен запрос на верификацию с кодом: " + verificationCode);
            
            if (verificationCode == null || verificationCode.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Код верификации не может быть пустым"
                ));
            }
            
            boolean verified = authService.verifyUser(verificationCode);
            
            if (verified) {
                System.out.println("✅ Верификация успешна");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Верификация прошла успешно"
                ));
            } else {
                System.out.println("❌ Ошибка верификации: неверный или устаревший код");
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Неверный или устаревший код верификации"
                ));
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка верификации: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Получение информации о пользователе
     */
    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("🔍 Получен запрос на получение информации о пользователе");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Требуется авторизация"
                ));
            }
            
            String token = authHeader.substring(7);
            String userEmail = authService.extractEmailFromToken(token);
            
            if (userEmail == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Недействительный токен"
                ));
            }
            
            User user = userRepository.findByEmail(userEmail)
                    .orElse(null);
            
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Пользователь не найден"
                ));
            }
            
            boolean hasSubscription = subscriptionService.hasActiveSubscription(user);
            boolean hasApiKey = user.getWildberriesApiKey() != null && !user.getWildberriesApiKey().trim().isEmpty();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                    "lastName", user.getLastName() != null ? user.getLastName() : "",
                    "phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                    "isVerified", user.getIsVerified(),
                    "hasApiKey", hasApiKey,
                    "hasSubscription", hasSubscription
                )
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения информации о пользователе: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка получения информации о пользователе: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Обновление профиля пользователя
     */
    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            System.out.println("✏️ Получен запрос на обновление профиля");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Требуется авторизация"
                ));
            }
            
            String token = authHeader.substring(7);
            String userEmail = authService.extractEmailFromToken(token);
            
            if (userEmail == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Недействительный токен"
                ));
            }
            
            User user = userRepository.findByEmail(userEmail)
                    .orElse(null);
            
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Пользователь не найден"
                ));
            }
            
            // Обновляем данные пользователя
            if (request.containsKey("firstName")) {
                user.setFirstName(request.get("firstName"));
            }
            
            if (request.containsKey("lastName")) {
                user.setLastName(request.get("lastName"));
            }
            
            if (request.containsKey("phoneNumber")) {
                user.setPhoneNumber(request.get("phoneNumber"));
            }
            
            userRepository.save(user);
            
            System.out.println("✅ Профиль пользователя обновлен успешно: " + userEmail);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Профиль успешно обновлен"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка обновления профиля: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка обновления профиля: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Изменение пароля пользователя
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            System.out.println("🔐 Получен запрос на изменение пароля");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Требуется авторизация"
                ));
            }
            
            String token = authHeader.substring(7);
            String userEmail = authService.extractEmailFromToken(token);
            
            if (userEmail == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Недействительный токен"
                ));
            }
            
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            
            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Необходимо указать текущий и новый пароль"
                ));
            }
            
            boolean changed = authService.changePassword(userEmail, currentPassword, newPassword);
            
            if (changed) {
                System.out.println("✅ Пароль изменен успешно: " + userEmail);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Пароль успешно изменен"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Неверный текущий пароль"
                ));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка изменения пароля: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка изменения пароля: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Получение информации о текущем API ключе
     */
    @GetMapping("/api-key")
    public ResponseEntity<?> getApiKey(@RequestParam(required = false) String email, 
                                     @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Извлекаем email из параметра или токена
            String userEmail = email;
            
            // Если email не указан в параметре, пробуем извлечь из токена
            if (userEmail == null && authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                userEmail = authService.extractEmailFromToken(token);
            }
            
            // Если не удалось получить email, возвращаем ошибку
            if (userEmail == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Требуется указать email или авторизоваться"
                ));
            }
            
            System.out.println("🔍 Getting API key for user: " + userEmail);
            
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            // Проверяем наличие активной подписки
            boolean hasSubscription = subscriptionService.hasActiveSubscription(user);
            if (!hasSubscription) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Для доступа к API ключу требуется активная подписка",
                    "requiresSubscription", true
                ));
            }
            
            boolean hasApiKey = user.getWildberriesApiKey() != null && !user.getWildberriesApiKey().trim().isEmpty();
            String apiKey = hasApiKey ? user.getWildberriesApiKey() : "";
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "hasApiKey", hasApiKey,
                "apiKey", apiKey
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error getting API key: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка получения API ключа: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Установка API ключа Wildberries
     */
    @PostMapping("/set-api-key")
    public ResponseEntity<?> setApiKey(@RequestParam(required = false) String email, 
                                     @RequestHeader(value = "Authorization", required = false) String authHeader,
                                     @RequestBody Map<String, String> request) {
        try {
            // Извлекаем email из параметра или токена
            String userEmail = email;
            
            // Если email не указан в параметре, пробуем извлечь из токена
            if (userEmail == null && authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                userEmail = authService.extractEmailFromToken(token);
            }
            
            // Если не удалось получить email, возвращаем ошибку
            if (userEmail == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Требуется указать email или авторизоваться"
                ));
            }
            
            System.out.println("🔍 Setting API key for user: " + userEmail);
            
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            // Проверяем наличие активной подписки
            boolean hasSubscription = subscriptionService.hasActiveSubscription(user);
            if (!hasSubscription) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Для установки API ключа требуется активная подписка",
                    "requiresSubscription", true
                ));
            }
            
            String apiKey = request.get("apiKey");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "API ключ не может быть пустым"
                ));
            }
            
            user.setWildberriesApiKey(apiKey.trim());
            userRepository.save(user);
            
            System.out.println("✅ API key set successfully for user: " + userEmail);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "API ключ Wildberries успешно установлен"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error setting API key: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка установки API ключа: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Удаление API ключа
     */
    @DeleteMapping("/api-key")
    public ResponseEntity<?> deleteApiKey(@RequestParam(required = false) String email,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Извлекаем email из параметра или токена
            String userEmail = email;
            
            // Если email не указан в параметре, пробуем извлечь из токена
            if (userEmail == null && authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                userEmail = authService.extractEmailFromToken(token);
            }
            
            // Если не удалось получить email, возвращаем ошибку
            if (userEmail == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Требуется указать email или авторизоваться"
                ));
            }
            
            System.out.println("🔍 Removing API key for user: " + userEmail);
            
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            user.setWildberriesApiKey(null);
            userRepository.save(user);
            
            System.out.println("✅ API key removed successfully for user: " + userEmail);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "API ключ удален"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error removing API key: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка удаления API ключа: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Проверка токена - публичный эндпоинт для проверки валидности JWT токена
     */
    @GetMapping("/check-token")
    public ResponseEntity<?> checkToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("🔍 Проверка токена");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "hasValidToken", false,
                    "tokenExpired", false,
                    "message", "Токен отсутствует"
                ));
            }
            
            String token = authHeader.substring(7);
            boolean isValid = jwtService.validateTokenSafely(token);
            
            if (isValid) {
                String userEmail = jwtService.extractUsername(token);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "hasValidToken", true,
                    "tokenExpired", false,
                    "userEmail", userEmail,
                    "message", "Токен действителен"
                ));
            } else {
                // Попробуем извлечь email из истекшего токена
                String userEmail = jwtService.extractEmailFromExpiredToken(token);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "hasValidToken", false,
                    "tokenExpired", true,
                    "userEmail", userEmail != null ? userEmail : "",
                    "message", "Токен истек"
                ));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка проверки токена: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "hasValidToken", false,
                "tokenExpired", false,
                "message", "Ошибка проверки токена"
            ));
        }
    }
} 