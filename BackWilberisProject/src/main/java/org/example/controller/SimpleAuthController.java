package org.example.controller;

import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/simple-auth")
@CrossOrigin(origins = "http://localhost:3000")
public class SimpleAuthController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public SimpleAuthController(UserRepository userRepository, 
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> simpleLogin(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            System.out.println("🔐 Simple login attempt for: " + email);
            
            // Ищем пользователя в базе
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                System.out.println("❌ User not found: " + email);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Пользователь не найден"
                ));
            }
            
            User user = userOpt.get();
            System.out.println("✅ User found: " + user.getEmail());
            
            // Проверяем пароль
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            System.out.println("🔑 Password check: " + passwordMatches);
            
            if (!passwordMatches) {
                System.out.println("❌ Wrong password");
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Неверный пароль"
                ));
            }
            
            // Генерируем JWT токен
            String token = jwtService.generateToken(user);
            System.out.println("✅ JWT token generated");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Авторизация успешна",
                "token", token,
                "user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "isVerified", user.getIsVerified(),
                    "hasWbApiKey", user.getWbApiKey() != null
                )
            ));
            
        } catch (Exception e) {
            System.out.println("❌ Simple login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Ошибка авторизации: " + e.getMessage()
            ));
        }
    }
} 
 
 