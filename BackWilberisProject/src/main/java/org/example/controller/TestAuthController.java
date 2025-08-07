package org.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "http://localhost:3000")
public class TestAuthController {
    
    @PostMapping("/ping")
    public ResponseEntity<?> ping(@RequestBody Map<String, Object> body) {
        System.out.println("🔍 Test ping received: " + body);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Test controller works",
            "received", body
        ));
    }
    
    @GetMapping("/simple")
    public ResponseEntity<?> simple() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Simple GET works"
        ));
    }
} 
 
 