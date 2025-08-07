package org.example.service;

import org.example.dto.admin.*;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    // Получение статистики системы
    public AdminStatsDto getSystemStats() {
        try {
            System.out.println("📊 AdminService: Получение статистики системы");
            
            List<User> allUsers = userRepository.findAll();
            
            int totalUsers = allUsers.size();
            int verifiedUsers = (int) allUsers.stream().filter(User::isVerified).count();
            int usersWithApiKeys = (int) allUsers.stream()
                .filter(u -> u.getWildberriesApiKey() != null && !u.getWildberriesApiKey().isEmpty())
                .count();
            
            // Пока что статичные данные для API вызовов и активности
            int totalApiCalls = 1247;
            int activeUsers24h = 5;
            int activeUsers7d = 11;
            
            Map<String, Integer> planDistribution = new HashMap<>();
            planDistribution.put("free", Math.max(0, totalUsers - 5));
            planDistribution.put("pro", 4);
            planDistribution.put("enterprise", 1);
            
            AdminStatsDto stats = new AdminStatsDto(
                totalUsers, 
                verifiedUsers, 
                usersWithApiKeys, 
                totalApiCalls, 
                activeUsers24h, 
                activeUsers7d, 
                planDistribution
            );
            
            System.out.println("✅ AdminService: Статистика собрана - " + totalUsers + " пользователей, " + verifiedUsers + " верифицированных");
            return stats;
            
        } catch (Exception e) {
            System.out.println("❌ AdminService: Ошибка получения статистики: " + e.getMessage());
            throw new RuntimeException("Ошибка получения статистики: " + e.getMessage());
        }
    }

    // Получение пользователей с пагинацией и поиском
    public Page<AdminUserDto> getUsers(Pageable pageable, String search) {
        try {
            System.out.println("👥 AdminService: Получение пользователей, поиск: " + search);
            
            List<User> allUsers = userRepository.findAll();
            
            // Фильтрация по поисковому запросу
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                allUsers = allUsers.stream()
                    .filter(user -> 
                        user.getEmail().toLowerCase().contains(searchLower) ||
                        user.getFirstName().toLowerCase().contains(searchLower) ||
                        user.getLastName().toLowerCase().contains(searchLower)
                    )
                    .collect(Collectors.toList());
            }
            
            // Преобразование в DTO
            List<AdminUserDto> userDtos = allUsers.stream()
                .map(this::convertToAdminUserDto)
                .collect(Collectors.toList());
            
            // Пагинация
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), userDtos.size());
            
            List<AdminUserDto> pageContent = userDtos.subList(start, end);
            
            System.out.println("✅ AdminService: Получено " + pageContent.size() + " пользователей из " + userDtos.size());
            return new PageImpl<>(pageContent, pageable, userDtos.size());
            
        } catch (Exception e) {
            System.out.println("❌ AdminService: Ошибка получения пользователей: " + e.getMessage());
            throw new RuntimeException("Ошибка получения пользователей: " + e.getMessage());
        }
    }

    // Обновление пользователя
    public AdminUserDto updateUser(Long userId, AdminUserUpdateDto updateDto) {
        try {
            System.out.println("📝 AdminService: Обновление пользователя ID: " + userId);
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            // Обновляем поля
            if (updateDto.getVerified() != null) {
                user.setIsVerified(updateDto.getVerified());
            }
            if (updateDto.getFirstName() != null) {
                user.setFirstName(updateDto.getFirstName());
            }
            if (updateDto.getLastName() != null) {
                user.setLastName(updateDto.getLastName());
            }
            
            user = userRepository.save(user);
            
            System.out.println("✅ AdminService: Пользователь обновлен: " + user.getEmail());
            return convertToAdminUserDto(user);
            
        } catch (Exception e) {
            System.out.println("❌ AdminService: Ошибка обновления пользователя: " + e.getMessage());
            throw new RuntimeException("Ошибка обновления пользователя: " + e.getMessage());
        }
    }

    // Удаление пользователя
    public void deleteUser(Long userId) {
        try {
            System.out.println("🗑️ AdminService: Удаление пользователя ID: " + userId);
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            userRepository.delete(user);
            
            System.out.println("✅ AdminService: Пользователь удален: " + user.getEmail());
            
        } catch (Exception e) {
            System.out.println("❌ AdminService: Ошибка удаления пользователя: " + e.getMessage());
            throw new RuntimeException("Ошибка удаления пользователя: " + e.getMessage());
        }
    }

    // Получение API логов
    public Page<AdminApiLogDto> getApiLogs(Pageable pageable, Long userId) {
        try {
            System.out.println("📋 AdminService: Получение API логов для пользователя: " + userId);
            
            // Пока что возвращаем тестовые данные
            List<AdminApiLogDto> logs = new ArrayList<>();
            
            // Тестовые данные
            logs.add(new AdminApiLogDto(1L, 1L, "admin@test.com", "/analytics/financial-report", "GET", 200, 245L, LocalDateTime.now().minusHours(1), null));
            logs.add(new AdminApiLogDto(2L, 1L, "admin@test.com", "/analytics/user-info", "GET", 200, 180L, LocalDateTime.now().minusHours(2), null));
            logs.add(new AdminApiLogDto(3L, 2L, "user@test.com", "/auth/login", "POST", 200, 120L, LocalDateTime.now().minusHours(3), null));
            
            // Фильтрация по пользователю
            if (userId != null) {
                logs = logs.stream()
                    .filter(log -> log.getUserId().equals(userId))
                    .collect(Collectors.toList());
            }
            
            // Пагинация
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), logs.size());
            
            List<AdminApiLogDto> pageContent = logs.subList(start, end);
            
            System.out.println("✅ AdminService: Получено " + pageContent.size() + " записей логов");
            return new PageImpl<>(pageContent, pageable, logs.size());
            
        } catch (Exception e) {
            System.out.println("❌ AdminService: Ошибка получения логов: " + e.getMessage());
            throw new RuntimeException("Ошибка получения логов: " + e.getMessage());
        }
    }

    // Получение системной информации
    public Map<String, Object> getSystemInfo() {
        try {
            System.out.println("⚙️ AdminService: Получение системной информации");
            
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("appVersion", "1.0.0");
            systemInfo.put("serverStatus", "Running");
            systemInfo.put("uptime", "24h 15m");
            systemInfo.put("dbType", "PostgreSQL");
            systemInfo.put("dbStatus", "Connected");
            systemInfo.put("dbSize", "2.3 MB");
            
            System.out.println("✅ AdminService: Системная информация получена");
            return systemInfo;
            
        } catch (Exception e) {
            System.out.println("❌ AdminService: Ошибка получения системной информации: " + e.getMessage());
            throw new RuntimeException("Ошибка получения системной информации: " + e.getMessage());
        }
    }

    // Получение юнит-экономики
    public UnitEconomicsDto getUnitEconomics(String userEmail, int days) {
        try {
            System.out.println("📈 AdminService: Получение юнит-экономики для пользователя: " + userEmail + " за " + days + " дней");
            
            // Тестовые данные на основе Excel таблицы
            List<UnitEconomicsDto.UnitEconomicsProductDto> products = new ArrayList<>();
            
            // Создаем тестовые продукты
            for (int i = 1; i <= 5; i++) {
                UnitEconomicsDto.UnitEconomicsProductDto product = new UnitEconomicsDto.UnitEconomicsProductDto();
                product.setId((long) i);
                product.setNmId(233743119L + i);
                product.setVendorCode("ART10000" + i);
                product.setBrandName("Brand " + i);
                
                // Данные из Excel
                product.setCostPrice(520);
                product.setDeliveryToCostPrice(50);
                product.setGrossProfit(367);
                product.setMpPriceBefore(3300);
                product.setSppDiscount(50);
                product.setPriceAfterSpp(1650);
                product.setBreakEvenPoint(22);
                product.setBuyout(1295);
                product.setMpCommissionPercent(24.5);
                product.setHeight(38);
                product.setWidth(9.5);
                product.setLength(3);
                product.setLogisticsMp(94);
                product.setLogisticsWithBuyout(238.2);
                product.setLogisticsFinal(238);
                product.setMpCommissionRub(404);
                product.setTotalMp(642);
                product.setToPay(1008);
                product.setTax(71);
                product.setRevenueAfterTax(937);
                product.setFinalGrossProfit(367);
                product.setMarkupPercent(68);
                product.setMarginality(22);
                product.setProfitabilityGross(64);
                product.setRoi(70 + i * 5); // Вариация для разных товаров
                
                products.add(product);
            }
            
            // Создаем сводку
            UnitEconomicsDto.UnitEconomicsSummaryDto summary = new UnitEconomicsDto.UnitEconomicsSummaryDto(
                products.size(),
                products.stream().mapToDouble(p -> p.getRevenueAfterTax()).sum(),
                products.stream().mapToDouble(p -> p.getFinalGrossProfit()).sum(),
                products.stream().mapToDouble(p -> p.getTotalMp()).sum(),
                products.stream().mapToDouble(p -> p.getMarginality()).average().orElse(0),
                products.stream().mapToDouble(p -> p.getRoi()).average().orElse(0)
            );
            
            UnitEconomicsDto result = new UnitEconomicsDto(products, summary);
            
            System.out.println("✅ AdminService: Юнит-экономика получена - " + products.size() + " товаров");
            return result;
            
        } catch (Exception e) {
            System.out.println("❌ AdminService: Ошибка получения юнит-экономики: " + e.getMessage());
            throw new RuntimeException("Ошибка получения юнит-экономики: " + e.getMessage());
        }
    }

    // Вспомогательный метод для преобразования User в AdminUserDto
    private AdminUserDto convertToAdminUserDto(User user) {
        return new AdminUserDto(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.isVerified(),
            user.getWbApiKey() != null && !user.getWbApiKey().isEmpty(),
            user.getCreatedAt(),
            null, // lastLoginAt пока что null, так как этого поля нет в User
            0, // Пока что 0 API вызовов
            "free" // Пока что все на free плане
        );
    }
} 
 
 