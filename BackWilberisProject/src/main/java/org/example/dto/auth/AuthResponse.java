package org.example.dto.auth;

public class AuthResponse {
    
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private boolean isVerified;
    private boolean hasWbApiKey;
    
    public AuthResponse() {}
    
    public AuthResponse(String token, Long userId, String email, String firstName, String lastName, boolean isVerified, boolean hasWbApiKey) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isVerified = isVerified;
        this.hasWbApiKey = hasWbApiKey;
    }
    
    // Геттеры
    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public boolean isVerified() { return isVerified; }
    public boolean isHasWbApiKey() { return hasWbApiKey; }
    
    // Сеттеры
    public void setToken(String token) { this.token = token; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setVerified(boolean isVerified) { this.isVerified = isVerified; }
    public void setHasWbApiKey(boolean hasWbApiKey) { this.hasWbApiKey = hasWbApiKey; }
} 