package org.example.config;

import org.example.service.JwtAuthenticationEntryPoint;
import org.example.service.JwtAuthenticationFilter;
import org.example.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("🔒 Configuring security filter chain...");
        
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                System.out.println("🔒 Configuring HTTP request authorization...");
                auth
                // Разрешаем OPTIONS запросы для всех эндпоинтов
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // ВЫСШИЙ ПРИОРИТЕТ: Публичные эндпоинты (ДОЛЖНЫ БЫТЬ ПЕРВЫМИ)
                .requestMatchers("/api/public/**").permitAll() // Все публичные API
                
                // Специальное разрешение для create-trial эндпоинта
                .requestMatchers(HttpMethod.POST, "/api/subscription/create-trial").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/subscription/create-trial/**").permitAll()
                
                // Отладочные эндпоинты
                .requestMatchers("/api/subscription/debug/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/subscription/debug/user-subscriptions").permitAll()
                .requestMatchers("/api/analytics/debug-test").permitAll() // 🧪 Тестовый endpoint
                
                // Публичные эндпоинты авторизации
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/verify").permitAll()
                .requestMatchers("/api/auth/check-token").permitAll() // Специальный эндпоинт для проверки токена
                
                // Остальные auth эндпоинты требуют авторизации
                .requestMatchers("/api/auth/**").authenticated()
                
                // Публичные эндпоинты подписок
                .requestMatchers("/api/subscription/plans").permitAll()
                .requestMatchers("/api/subscription/create-trial").permitAll()
                
                // Остальные subscription эндпоинты требуют авторизации  
                .requestMatchers("/api/subscription/**").authenticated()
                
                // Аналитика требует авторизации
                .requestMatchers("/api/analytics/**").authenticated()
                .requestMatchers("/api/test/**").permitAll() // Тестовые эндпоинты
                .requestMatchers("/api/debug/**").permitAll() // Отладочные эндпоинты
                // Все остальные требуют аутентификации
                .anyRequest().authenticated();
                
                System.out.println("✅ HTTP request authorization configured successfully");
            });
        
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        System.out.println("✅ Security filter chain configured successfully");
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Разрешаем конкретные источники вместо wildcard "*"
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000", 
            "http://localhost:3001", 
            "http://localhost:3002",
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:3000"
        ));
        
        // Разрешаем все методы HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Разрешаем ВСЕ заголовки (включая Authorization)
        // Явно разрешаем все необходимые заголовки (включая кастомные)
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type", 
            "Accept",
            "X-Frontend-Request-ID",
            "X-Debug-Source",
            "Cache-Control",
            "Pragma",
            "Expires",
            "X-Requested-With",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Разрешаем передачу учетных данных (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Устанавливаем время кэширования предварительных запросов CORS
        configuration.setMaxAge(3600L);
        
        // Разрешаем заголовки, которые клиент может использовать
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Disposition",
            "X-Request-ID",
            "Cache-Control"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        System.out.println("🔄 CORS configuration initialized:");
        System.out.println("  - Origins: " + configuration.getAllowedOrigins());
        System.out.println("  - Methods: " + configuration.getAllowedMethods());
        System.out.println("  - Headers: " + configuration.getAllowedHeaders());
        System.out.println("  - Credentials: " + configuration.getAllowCredentials());
        System.out.println("  - Max Age: " + configuration.getMaxAge());
        
        return source;
    }
} 