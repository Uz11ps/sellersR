package org.example.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    
    // Список эндпоинтов, которые не требуют JWT аутентификации
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "/api/auth/",
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/verify",
        "/api/auth/api-key",
        "/api/auth/user-info",
        "/api/public/",
        "/api/public/subscription/free",
        "/api/public/subscription/debug",
        "/api/subscription/plans",
        "/api/subscription/info",
        "/api/subscription/create",
        "/api/subscription/cancel",
        "/api/subscription/create-trial",
        "/api/test/",
        "/api/debug/"
    );
    
    // Список эндпоинтов, которые требуют особого внимания
    private static final List<String> CRITICAL_ENDPOINTS = Arrays.asList(
        "/api/subscription/create-trial",
        "/api/subscription/create",
        "/api/subscription/cancel"
    );
    
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        logger.info("🔒 JwtAuthenticationFilter initialized with PUBLIC_ENDPOINTS: {}", PUBLIC_ENDPOINTS);
        logger.info("🔍 JwtAuthenticationFilter initialized with CRITICAL_ENDPOINTS: {}", CRITICAL_ENDPOINTS);
    }
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();
        
        // Добавляем заголовки для предотвращения кэширования
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // Добавляем заголовки CORS для всех запросов
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Request-ID, Cache-Control, Pragma, Expires");
        
        // Добавляем логирование для OPTIONS запросов
        if (method.equals("OPTIONS")) {
            logger.info("🔍 Preflight OPTIONS request for: {}", requestURI);
            // Для OPTIONS запросов всегда пропускаем фильтр
            filterChain.doFilter(request, response);
            return;
        }
        
        // Очищаем URL от параметров для проверки
        String cleanUri = requestURI;
        if (cleanUri.contains("?")) {
            cleanUri = cleanUri.substring(0, cleanUri.indexOf("?"));
        }
        
        // Проверяем, содержит ли URL параметр _t
        String queryString = request.getQueryString();
        if (queryString != null && queryString.contains("_t=")) {
            logger.warn("⚠️ URL содержит параметр _t: {}", queryString);
            
            // Если это create-trial, то пропускаем запрос без проверки токена
            if (cleanUri.endsWith("/api/subscription/create-trial")) {
                logger.info("🔓 Пропускаем запрос к create-trial с параметром _t без проверки токена");
                filterChain.doFilter(request, response);
                return;
            }
        }
        
        // ПЕРВАЯ проверка - безусловный bypass для ВСЕХ публичных эндпоинтов
        if (cleanUri.startsWith("/api/public/")) {
            logger.info("🌍 BYPASS JWT for PUBLIC endpoint: {}", cleanUri);
            filterChain.doFilter(request, response);
            return;
        }
        
        // ВТОРАЯ проверка - безусловный bypass для create-trial
        if (cleanUri.endsWith("/api/subscription/create-trial")) {
            logger.info("🎁 BYPASS JWT for create-trial endpoint (FIRST CHECK)");
            filterChain.doFilter(request, response);
            return;
        }
        
        // ТРЕТЬЯ проверка - безусловный bypass для debug эндпоинтов
        if (cleanUri.startsWith("/api/subscription/debug/")) {
            logger.info("🔍 BYPASS JWT for debug endpoint: {}", cleanUri);
            filterChain.doFilter(request, response);
            return;
        }
        
        // Более подробное логирование для критических эндпоинтов
        boolean isCriticalEndpoint = CRITICAL_ENDPOINTS.stream()
            .anyMatch(cleanUri::endsWith);
            
        if (isCriticalEndpoint) {
            logger.info("⚠️ CRITICAL ENDPOINT: {} {}", method, cleanUri);
        } else {
            logger.info("🔍 JWT Filter: {} {}", method, cleanUri);
        }
        
        // Проверяем, является ли эндпоинт публичным
        boolean isPublicEndpoint = false;
        
        // Проверяем точные совпадения
        if (PUBLIC_ENDPOINTS.contains(cleanUri)) {
            isPublicEndpoint = true;
            logger.info("🔓 Exact match for public endpoint: {}", cleanUri);
        } 
        // Проверяем префиксы
        else {
            for (String prefix : PUBLIC_ENDPOINTS) {
                if (prefix.endsWith("/") && cleanUri.startsWith(prefix)) {
                    isPublicEndpoint = true;
                    logger.info("🔓 Prefix match for public endpoint: {} with prefix {}", cleanUri, prefix);
                    break;
                }
            }
        }
        
        // Специальная проверка для конкретных эндпоинтов
        if (cleanUri.startsWith("/api/auth/") || 
            cleanUri.startsWith("/api/public/") || 
            cleanUri.equals("/api/subscription/plans") ||
            cleanUri.equals("/api/subscription/info") ||
            cleanUri.equals("/api/subscription/create") ||
            cleanUri.equals("/api/subscription/cancel") ||
            cleanUri.equals("/api/subscription/create-trial") ||
            cleanUri.equals("/api/auth/api-key") ||
            cleanUri.equals("/api/auth/user-info")) {
            isPublicEndpoint = true;
            logger.info("🔓 Special case: public endpoint: {}", cleanUri);
        }
        
        // Пропускаем публичные эндпоинты
        if (isPublicEndpoint) {
            logger.info("🔓 BYPASSING JWT for: {}", cleanUri);
            filterChain.doFilter(request, response);
            return;
        }
        
        final String authHeader = request.getHeader("Authorization");
        logger.info("🔑 Authorization header: {}", authHeader != null ? "PRESENT" : "MISSING");
        
        if (authHeader != null) {
            logger.info("🔑 Header value: {}", authHeader.substring(0, Math.min(authHeader.length(), 50)) + "...");
        }
        
        final String jwt;
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("❌ NO VALID BEARER TOKEN for: {} {}", method, cleanUri);
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            jwt = authHeader.substring(7);
            logger.info("🔑 Extracted JWT token: {}...", jwt.substring(0, Math.min(jwt.length(), 30)));
            
            // Сначала проверяем, валиден ли токен, без выбрасывания исключения
            if (jwtService.validateTokenSafely(jwt)) {
                // Если токен валиден, продолжаем стандартную обработку
                String userEmail = jwtService.extractUsername(jwt);
                logger.info("📧 Extracted email from token: {}", userEmail);
                
                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    logger.info("👤 Loading user details for: {}", userEmail);
                    
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                    logger.info("✅ User details loaded successfully");
                    
                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        logger.info("🎯 TOKEN IS VALID - Setting authentication");
                        
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        
                        logger.info("✅ AUTHENTICATION SET SUCCESSFULLY for: {}", userEmail);
                    } else {
                        logger.error("❌ TOKEN IS INVALID for user: {}", userEmail);
                    }
                }
            } else {
                // Если токен истек, пытаемся извлечь email из истекшего токена
                String userEmail = jwtService.extractEmailFromExpiredToken(jwt);
                
                if (userEmail != null) {
                    logger.info("⏰ Token expired for user: {}", userEmail);
                    
                    // Добавляем информацию о истекшем токене в атрибуты запроса
                    // Это позволит контроллерам определить, что токен истек и предпринять соответствующие действия
                    request.setAttribute("expiredToken", true);
                    request.setAttribute("expiredTokenEmail", userEmail);
                } else {
                    logger.error("❌ Could not extract email from expired token");
                }
            }
        } catch (Exception e) {
            logger.error("❌ CRITICAL ERROR processing JWT token: {}", e.getMessage(), e);
        }
        
        // Проверяем финальное состояние аутентификации
        boolean isAuthenticated = SecurityContextHolder.getContext().getAuthentication() != null;
        logger.info("🏁 Final authentication status for {}: {}", cleanUri, isAuthenticated ? "AUTHENTICATED" : "NOT AUTHENTICATED");
        
        filterChain.doFilter(request, response);
    }
} 
 
 