package org.example.controller;

import org.example.dto.admin.*;
import org.example.entity.User;
import org.example.service.AdminService;
import org.example.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtService jwtService;

    // Получение статистики системы
    @GetMapping("/stats")
    public ResponseEntity<AdminApiResponse<AdminStatsDto>> getStats(HttpServletRequest request) {
        try {
            System.out.println("📊 Admin: Запрос статистики системы");
            
            // Проверяем авторизацию
            String token = jwtService.extractTokenFromRequest(request);
            if (token == null) {
                System.out.println("❌ Admin: Токен не найден");
                return ResponseEntity.ok(new AdminApiResponse<>(false, "Не авторизован", null));
            }

            String userEmail = jwtService.extractUsername(token);
            if (userEmail == null) {
                System.out.println("❌ Admin: Email не извлечен из токена");
                return ResponseEntity.ok(new AdminApiResponse<>(false, "Неверный токен", null));
            }

            // Получаем статистику
            AdminStatsDto stats = adminService.getSystemStats();
            System.out.println("✅ Admin: Статистика получена: " + stats.getTotalUsers() + " пользователей");
            
            return ResponseEntity.ok(new AdminApiResponse<>(true, "Статистика получена", stats));
            
        } catch (Exception e) {
            System.out.println("❌ Admin: Ошибка получения статистики: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new AdminApiResponse<>(false, "Ошибка сервера: " + e.getMessage(), null));
        }
    }

    // Получение списка пользователей
    @GetMapping("/users")
    public ResponseEntity<AdminApiResponse<Page<AdminUserDto>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            HttpServletRequest request) {
        try {
            System.out.println("👥 Admin: Запрос пользователей, страница: " + page + ", размер: " + size + ", поиск: " + search);
            
            // Проверяем авторизацию
            String token = jwtService.extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.ok(new AdminApiResponse<>(false, "Не авторизован", null));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<AdminUserDto> users = adminService.getUsers(pageable, search);
            
            System.out.println("✅ Admin: Получено " + users.getContent().size() + " пользователей");
            return ResponseEntity.ok(new AdminApiResponse<>(true, "Пользователи получены", users));
            
        } catch (Exception e) {
            System.out.println("❌ Admin: Ошибка получения пользователей: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new AdminApiResponse<>(false, "Ошибка сервера: " + e.getMessage(), null));
        }
    }

    // Обновление пользователя
    @PutMapping("/users/{userId}")
    public ResponseEntity<AdminApiResponse<AdminUserDto>> updateUser(
            @PathVariable Long userId,
            @RequestBody AdminUserUpdateDto updateDto,
            HttpServletRequest request) {
        try {
            System.out.println("📝 Admin: Обновление пользователя ID: " + userId);
            
            // Проверяем авторизацию
            String token = jwtService.extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.ok(new AdminApiResponse<>(false, "Не авторизован", null));
            }

            AdminUserDto updatedUser = adminService.updateUser(userId, updateDto);
            System.out.println("✅ Admin: Пользователь обновлен: " + updatedUser.getEmail());
            
            return ResponseEntity.ok(new AdminApiResponse<>(true, "Пользователь обновлен", updatedUser));
            
        } catch (Exception e) {
            System.out.println("❌ Admin: Ошибка обновления пользователя: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new AdminApiResponse<>(false, "Ошибка сервера: " + e.getMessage(), null));
        }
    }

    // Удаление пользователя
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<AdminApiResponse<String>> deleteUser(
            @PathVariable Long userId,
            HttpServletRequest request) {
        try {
            System.out.println("🗑️ Admin: Удаление пользователя ID: " + userId);
            
            // Проверяем авторизацию
            String token = jwtService.extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.ok(new AdminApiResponse<>(false, "Не авторизован", null));
            }

            adminService.deleteUser(userId);
            System.out.println("✅ Admin: Пользователь удален");
            
            return ResponseEntity.ok(new AdminApiResponse<>(true, "Пользователь удален", null));
            
        } catch (Exception e) {
            System.out.println("❌ Admin: Ошибка удаления пользователя: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new AdminApiResponse<>(false, "Ошибка сервера: " + e.getMessage(), null));
        }
    }

    // Получение API логов
    @GetMapping("/api-logs")
    public ResponseEntity<AdminApiResponse<Page<AdminApiLogDto>>> getApiLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        try {
            System.out.println("📋 Admin: Запрос API логов, страница: " + page + ", пользователь: " + userId);
            
            // Проверяем авторизацию
            String token = jwtService.extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.ok(new AdminApiResponse<>(false, "Не авторизован", null));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<AdminApiLogDto> logs = adminService.getApiLogs(pageable, userId);
            
            System.out.println("✅ Admin: Получено " + logs.getContent().size() + " записей логов");
            return ResponseEntity.ok(new AdminApiResponse<>(true, "Логи получены", logs));
            
        } catch (Exception e) {
            System.out.println("❌ Admin: Ошибка получения логов: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new AdminApiResponse<>(false, "Ошибка сервера: " + e.getMessage(), null));
        }
    }

    // Получение системной информации
    @GetMapping("/system")
    public ResponseEntity<AdminApiResponse<Map<String, Object>>> getSystemInfo(HttpServletRequest request) {
        try {
            System.out.println("⚙️ Admin: Запрос системной информации");
            
            // Проверяем авторизацию
            String token = jwtService.extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.ok(new AdminApiResponse<>(false, "Не авторизован", null));
            }

            Map<String, Object> systemInfo = adminService.getSystemInfo();
            System.out.println("✅ Admin: Системная информация получена");
            
            return ResponseEntity.ok(new AdminApiResponse<>(true, "Системная информация получена", systemInfo));
            
        } catch (Exception e) {
            System.out.println("❌ Admin: Ошибка получения системной информации: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new AdminApiResponse<>(false, "Ошибка сервера: " + e.getMessage(), null));
        }
    }

    // Получение данных юнит-экономики
    @PostMapping("/unit-economics")
    public ResponseEntity<AdminApiResponse<UnitEconomicsDto>> getUnitEconomics(
            @RequestParam(value = "days", defaultValue = "30") int days,
            HttpServletRequest request) {
        try {
            System.out.println("💰 Admin: Запрос юнит-экономики за " + days + " дней");
            
            // Проверяем авторизацию
            String token = jwtService.extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.ok(new AdminApiResponse<>(false, "Не авторизован", null));
            }

            String userEmail = jwtService.extractUsername(token);
            UnitEconomicsDto unitEconomics = adminService.getUnitEconomics(userEmail, days);
            System.out.println("✅ Admin: Юнит-экономика получена");
            
            return ResponseEntity.ok(new AdminApiResponse<>(true, "Юнит-экономика получена", unitEconomics));
            
        } catch (Exception e) {
            System.out.println("❌ Admin: Ошибка получения юнит-экономики: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new AdminApiResponse<>(false, "Ошибка сервера: " + e.getMessage(), null));
        }
    }
} 
 
 