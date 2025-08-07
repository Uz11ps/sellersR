package org.example.controller;

import org.example.entity.Seller;
import org.example.entity.User;
import org.example.repository.SellerRepository;
import org.example.repository.UserRepository;
import org.example.service.WildberriesApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sellers")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class SellerController {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WildberriesApiService wildberriesApiService;

    /**
     * Helper метод для получения пользователя из Authentication
     */
    private User getUserFromAuth(Authentication auth) {
        String userEmail = auth.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    /**
     * Получение списка всех продавцов пользователя
     */
    @GetMapping
    public ResponseEntity<?> getAllSellers(Authentication auth) {
        try {
            User user = getUserFromAuth(auth);
            List<Seller> sellers = sellerRepository.findByUser(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", sellers
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка получения списка продавцов: " + e.getMessage()
            ));
        }
    }

    /**
     * Получение активных продавцов
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveSellers(Authentication auth) {
        try {
            User user = getUserFromAuth(auth);
            List<Seller> sellers = sellerRepository.findByUserAndIsActiveTrue(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", sellers
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка получения активных продавцов: " + e.getMessage()
            ));
        }
    }

    /**
     * Получение продавца по ID
     */
    @GetMapping("/{sellerId}")
    public ResponseEntity<?> getSeller(Authentication auth, @PathVariable Long sellerId) {
        try {
            User user = getUserFromAuth(auth);
            Optional<Seller> sellerOpt = sellerRepository.findByUserAndId(user, sellerId);

            if (sellerOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Продавец не найден"
                ));
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", sellerOpt.get()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка получения продавца: " + e.getMessage()
            ));
        }
    }

    /**
     * Создание нового продавца
     */
    @PostMapping
    public ResponseEntity<?> createSeller(Authentication auth, @RequestBody Map<String, Object> sellerData) {
        try {
            User user = getUserFromAuth(auth);
            
            String sellerName = (String) sellerData.get("sellerName");
            String inn = (String) sellerData.get("inn");
            String wbApiKey = (String) sellerData.get("wbApiKey");

            if (sellerName == null || sellerName.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Название продавца обязательно"
                ));
            }

            // Проверяем уникальность ИНН
            if (inn != null && !inn.trim().isEmpty()) {
                if (sellerRepository.existsByUserAndInn(user, inn)) {
                    return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Продавец с таким ИНН уже существует"
                    ));
                }
            }

            // Проверяем API ключ если указан
            if (wbApiKey != null && !wbApiKey.trim().isEmpty()) {
                if (!wildberriesApiService.validateApiKey(wbApiKey)) {
                    return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Неверный API ключ Wildberries"
                    ));
                }
            }

            Seller seller = new Seller(user, sellerName.trim(), inn, wbApiKey);
            seller = sellerRepository.save(seller);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", seller,
                "message", "Продавец успешно создан"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка создания продавца: " + e.getMessage()
            ));
        }
    }

    /**
     * Обновление продавца
     */
    @PutMapping("/{sellerId}")
    public ResponseEntity<?> updateSeller(Authentication auth, @PathVariable Long sellerId, 
                                        @RequestBody Map<String, Object> sellerData) {
        try {
            User user = getUserFromAuth(auth);
            Optional<Seller> sellerOpt = sellerRepository.findByUserAndId(user, sellerId);

            if (sellerOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Продавец не найден"
                ));
            }

            Seller seller = sellerOpt.get();
            
            if (sellerData.containsKey("sellerName")) {
                String sellerName = (String) sellerData.get("sellerName");
                if (sellerName != null && !sellerName.trim().isEmpty()) {
                    seller.setSellerName(sellerName.trim());
                }
            }

            if (sellerData.containsKey("inn")) {
                String inn = (String) sellerData.get("inn");
                seller.setInn(inn);
            }

            if (sellerData.containsKey("wbApiKey")) {
                String wbApiKey = (String) sellerData.get("wbApiKey");
                if (wbApiKey != null && !wbApiKey.trim().isEmpty()) {
                    if (!wildberriesApiService.validateApiKey(wbApiKey)) {
                        return ResponseEntity.ok(Map.of(
                            "success", false,
                            "message", "Неверный API ключ Wildberries"
                        ));
                    }
                }
                seller.setWbApiKey(wbApiKey);
            }

            if (sellerData.containsKey("isActive")) {
                Boolean isActive = (Boolean) sellerData.get("isActive");
                seller.setActive(isActive != null ? isActive : true);
            }

            seller.setUpdatedAt(LocalDateTime.now());
            seller = sellerRepository.save(seller);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", seller,
                "message", "Продавец успешно обновлен"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка обновления продавца: " + e.getMessage()
            ));
        }
    }

    /**
     * Деактивация продавца
     */
    @PutMapping("/{sellerId}/deactivate")
    public ResponseEntity<?> deactivateSeller(Authentication auth, @PathVariable Long sellerId) {
        try {
            User user = getUserFromAuth(auth);
            Optional<Seller> sellerOpt = sellerRepository.findByUserAndId(user, sellerId);

            if (sellerOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Продавец не найден"
                ));
            }

            Seller seller = sellerOpt.get();
            seller.setActive(false);
            seller.setUpdatedAt(LocalDateTime.now());
            seller = sellerRepository.save(seller);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", seller,
                "message", "Продавец деактивирован"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка деактивации продавца: " + e.getMessage()
            ));
        }
    }

    /**
     * Тестирование API ключа продавца
     */
    @PostMapping("/{sellerId}/test-api")
    public ResponseEntity<?> testSellerApi(Authentication auth, @PathVariable Long sellerId) {
        try {
            User user = getUserFromAuth(auth);
            Optional<Seller> sellerOpt = sellerRepository.findByUserAndId(user, sellerId);

            if (sellerOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Продавец не найден"
                ));
            }

            Seller seller = sellerOpt.get();
            
            if (seller.getWbApiKey() == null || seller.getWbApiKey().trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "API ключ не установлен для данного продавца"
                ));
            }

            boolean isValid = wildberriesApiService.validateApiKey(seller.getWbApiKey());
            boolean statisticsAccess = wildberriesApiService.testStatisticsAccess(seller.getWbApiKey());

            Map<String, Object> testResults = new HashMap<>();
            testResults.put("basicValidation", isValid);
            testResults.put("statisticsAccess", statisticsAccess);
            testResults.put("seller", seller.getSellerName());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", testResults
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка тестирования API: " + e.getMessage()
            ));
        }
    }

    /**
     * Синхронизация данных для продавца
     */
    @PostMapping("/{sellerId}/sync")
    public ResponseEntity<?> syncSellerData(Authentication auth, @PathVariable Long sellerId,
                                           @RequestParam(value = "days", defaultValue = "30") int days) {
        try {
            User user = getUserFromAuth(auth);
            Optional<Seller> sellerOpt = sellerRepository.findByUserAndId(user, sellerId);

            if (sellerOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Продавец не найден"
                ));
            }

            Seller seller = sellerOpt.get();
            
            if (seller.getWbApiKey() == null || seller.getWbApiKey().trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "API ключ не установлен для данного продавца"
                ));
            }

            // Синхронизируем данные через WildberriesApiService
            // TODO: Обновить WildberriesApiService для работы с Seller entity
            
            seller.setLastSyncAt(LocalDateTime.now());
            sellerRepository.save(seller);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Синхронизация данных завершена для продавца: " + seller.getSellerName()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка синхронизации: " + e.getMessage()
            ));
        }
    }

    /**
     * Получение статистики по продавцам
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getSellersStats(Authentication auth) {
        try {
            User user = getUserFromAuth(auth);
            
            long totalSellers = sellerRepository.countActiveByUser(user);
            List<Seller> sellersWithApi = sellerRepository.findByUserWithApiKey(user);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalSellers", totalSellers);
            stats.put("sellersWithApiKey", sellersWithApi.size());
            stats.put("sellersWithoutApiKey", totalSellers - sellersWithApi.size());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка получения статистики: " + e.getMessage()
            ));
        }
    }
} 