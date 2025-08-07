package org.example.controller;

import org.example.entity.Subscription;
import org.example.entity.User;
import org.example.repository.SubscriptionRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    /**
     * Публичный эндпоинт для создания бесплатной подписки
     * Не требует JWT токена
     */
    @PostMapping("/subscription/free")
    public ResponseEntity<?> createFreeSubscription(@RequestBody Map<String, String> requestBody) {
        try {
            System.out.println("\n\n🎁 ===== ПУБЛИЧНОЕ СОЗДАНИЕ БЕСПЛАТНОЙ ПОДПИСКИ =====");
            System.out.println("📦 Request body: " + requestBody);
            
            String userEmail = requestBody.get("email");
            if (userEmail == null || userEmail.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Email не указан"
                ));
            }
            
            System.out.println("📧 Email: " + userEmail);
            
            // Найти пользователя по email
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (!userOpt.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Пользователь не найден"
                ));
            }
            
            User user = userOpt.get();
            System.out.println("✅ Пользователь найден: " + user.getEmail());
            
            // Проверить, есть ли уже активная подписка
            boolean hasActive = subscriptionRepository.findByUserAndStatus(user, Subscription.SubscriptionStatus.ACTIVE)
                .stream()
                .anyMatch(sub -> sub.getEndDate().isAfter(LocalDateTime.now()));
            
            if (hasActive) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "У пользователя уже есть активная подписка"
                ));
            }
            
            // Создать бесплатную подписку
            Subscription subscription = new Subscription();
            subscription.setUser(user);
            subscription.setPlanType(Subscription.PlanType.PLAN_FREE);
            subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
            subscription.setStartDate(LocalDateTime.now());
            subscription.setEndDate(LocalDateTime.now().plusDays(7)); // 7 дней бесплатно
            subscription.setPrice(0.0);
            subscription.setAutoRenew(false);
            subscription.setPaymentMethod("FREE");
            subscription.setCreatedAt(LocalDateTime.now());
            subscription.setUpdatedAt(LocalDateTime.now());
            
            subscription = subscriptionRepository.save(subscription);
            System.out.println("✅ Бесплатная подписка создана: " + subscription.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Бесплатная подписка успешно активирована");
            response.put("subscription", Map.of(
                "id", subscription.getId(),
                "planType", subscription.getPlanType().toString(),
                "startDate", subscription.getStartDate().toString(),
                "endDate", subscription.getEndDate().toString(),
                "status", subscription.getStatus().toString(),
                "price", subscription.getPrice()
            ));
            
            System.out.println("🎁 ===== ЗАВЕРШЕНИЕ СОЗДАНИЯ БЕСПЛАТНОЙ ПОДПИСКИ =====\n\n");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка создания бесплатной подписки: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка создания подписки: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Публичный отладочный эндпоинт для проверки подписок пользователя
     */
    @GetMapping("/subscription/debug")
    public ResponseEntity<?> debugUserSubscriptions(@RequestParam String email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (!userOpt.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Пользователь не найден"
                ));
            }
            
            User user = userOpt.get();
            var subscriptions = subscriptionRepository.findByUser(user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "email", email,
                "subscriptionsCount", subscriptions.size(),
                "subscriptions", subscriptions
            ));
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка: " + e.getMessage()
            ));
        }
    }
} 