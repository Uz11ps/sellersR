package org.example.controller;

import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.AuthService;
import org.example.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:5173"})
public class PublicApiController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SubscriptionService subscriptionService;
    
    @Autowired
    private AuthService authService;
    
    /**
     * Публичный метод для получения API ключа
     */
    @GetMapping("/api-key")
    public ResponseEntity<?> getApiKey(@RequestParam String email, @RequestParam(required = false) String token) {
        try {
            System.out.println("🔍 Public API: Getting API key for user: " + email);
            
            // Проверяем, существует ли пользователь
            User user = userRepository.findByEmail(email)
                    .orElse(null);
            
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Пользователь не найден"
                ));
            }
            
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
     * Публичный метод для установки API ключа
     */
    @PostMapping("/set-api-key")
    public ResponseEntity<?> setApiKey(
            @RequestParam(required = false) String email, 
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            // Извлекаем email из параметра или токена
            String userEmail = email;
            
            // Если email не указан в параметре, пробуем извлечь из токена
            if (userEmail == null && authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                userEmail = authService.extractEmailFromToken(token);
                System.out.println("🔐 Extracted email from token: " + userEmail);
            }
            
            // Если не удалось получить email, возвращаем ошибку
            if (userEmail == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Требуется указать email или авторизоваться"
                ));
            }
            
            System.out.println("🔍 Public API: Setting API key for user: " + userEmail);
            
            // Проверяем, существует ли пользователь
            User user = userRepository.findByEmail(userEmail)
                    .orElse(null);
            
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Пользователь не найден"
                ));
            }
            
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
            
            System.out.println("✅ Public API: API key set successfully for user: " + userEmail);
            
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
     * Публичный метод для удаления API ключа
     */
    @PostMapping("/remove-api-key")
    public ResponseEntity<?> removeApiKey(
            @RequestParam(required = false) String email,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Извлекаем email из параметра или токена
            String userEmail = email;
            
            // Если email не указан в параметре, пробуем извлечь из токена
            if (userEmail == null && authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                userEmail = authService.extractEmailFromToken(token);
                System.out.println("🔐 Extracted email from token: " + userEmail);
            }
            
            // Если не удалось получить email, возвращаем ошибку
            if (userEmail == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Требуется указать email или авторизоваться"
                ));
            }
            
            System.out.println("🔍 Public API: Removing API key for user: " + userEmail);
            
            // Проверяем, существует ли пользователь
            User user = userRepository.findByEmail(userEmail)
                    .orElse(null);
            
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Пользователь не найден"
                ));
            }
            
            user.setWildberriesApiKey(null);
            userRepository.save(user);
            
            System.out.println("✅ Public API: API key removed successfully for user: " + userEmail);
            
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
} 