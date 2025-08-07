package org.example.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;
    
    // Допустимое отклонение времени для токенов (5 минут)
    private static final long CLOCK_SKEW = 300000; // 5 минут в миллисекундах
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Метод для извлечения email из токена
     * Используется в контроллерах для ручной проверки токена
     */
    public String extractEmailFromToken(String token) {
        try {
            return extractUsername(token);
        } catch (Exception e) {
            System.err.println("❌ Error extracting email from token: " + e.getMessage());
            return null;
        }
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }
    
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }
    
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    
    /**
     * Проверяет валидность токена без выбрасывания исключения
     * @param token JWT токен
     * @return true если токен валиден, false если истек или невалиден
     */
    public boolean validateTokenSafely(String token) {
        try {
            // Проверяем, что токен можно распарсить и он не истек
            Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .setAllowedClockSkewSeconds(CLOCK_SKEW / 1000) // Устанавливаем допустимое отклонение времени
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("⏰ Token has expired: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("❌ Invalid token: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Извлекает email из токена даже если он истек
     * @param token JWT токен
     * @return email пользователя или null если токен невалиден
     */
    public String extractEmailFromExpiredToken(String token) {
        try {
            return extractUsername(token);
        } catch (ExpiredJwtException e) {
            // Извлекаем email из истекшего токена
            return e.getClaims().getSubject();
        } catch (Exception e) {
            System.err.println("❌ Error extracting email from token: " + e.getMessage());
            return null;
        }
    }
    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    private Key getSignInKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String extractTokenFromRequest(jakarta.servlet.http.HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 