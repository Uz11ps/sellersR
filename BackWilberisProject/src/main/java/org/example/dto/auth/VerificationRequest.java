package org.example.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class VerificationRequest {
    
    @NotBlank(message = "Код верификации обязателен")
    private String verificationCode;
    
    // Геттеры и сеттеры
    public String getVerificationCode() {
        return verificationCode;
    }
    
    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
} 