package org.example.dto.admin;

import java.time.LocalDateTime;

public class AdminUserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean verified;
    private boolean hasWbApiKey;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private int totalApiCalls;
    private String plan;

    public AdminUserDto() {}

    public AdminUserDto(Long id, String email, String firstName, String lastName, 
                       boolean verified, boolean hasWbApiKey, LocalDateTime createdAt, 
                       LocalDateTime lastLoginAt, int totalApiCalls, String plan) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.verified = verified;
        this.hasWbApiKey = hasWbApiKey;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.totalApiCalls = totalApiCalls;
        this.plan = plan;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isHasWbApiKey() {
        return hasWbApiKey;
    }

    public void setHasWbApiKey(boolean hasWbApiKey) {
        this.hasWbApiKey = hasWbApiKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public int getTotalApiCalls() {
        return totalApiCalls;
    }

    public void setTotalApiCalls(int totalApiCalls) {
        this.totalApiCalls = totalApiCalls;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }
} 
 
 