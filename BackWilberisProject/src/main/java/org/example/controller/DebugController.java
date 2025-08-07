package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "http://localhost:3000")
public class DebugController {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/login")
    public ResponseEntity<?> debugLogin(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        System.out.println("🔍 DEBUG LOGIN - URI: " + request.getRequestURI());
        System.out.println("🔍 DEBUG LOGIN - Method: " + request.getMethod());
        System.out.println("🔍 DEBUG LOGIN - Content-Type: " + request.getContentType());
        System.out.println("🔍 DEBUG LOGIN - Body: " + body);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Debug login works",
            "uri", request.getRequestURI(),
            "method", request.getMethod(),
            "body", body
        ));
    }
    
    @GetMapping("/status")
    public ResponseEntity<?> status() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Debug controller is working",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/db-connection")
    public ResponseEntity<?> checkDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Проверяем подключение напрямую через DataSource
            try (Connection connection = dataSource.getConnection()) {
                response.put("connectionSuccess", true);
                response.put("dbProduct", connection.getMetaData().getDatabaseProductName());
                response.put("dbVersion", connection.getMetaData().getDatabaseProductVersion());
                response.put("url", connection.getMetaData().getURL());
                response.put("username", connection.getMetaData().getUserName());
            }
            
            // Проверяем выполнение запроса через JdbcTemplate
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("queryResult", result);
            
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            response.put("connectionSuccess", false);
            response.put("error", e.getMessage());
            response.put("sqlState", e.getSQLState());
            response.put("errorCode", e.getErrorCode());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("connectionSuccess", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.ok(response);
        }
    }
} 
 
 