package org.example.dto.admin;

import java.time.LocalDateTime;

public class AdminApiLogDto {
    private Long id;
    private Long userId;
    private String userEmail;
    private String endpoint;
    private String method;
    private int statusCode;
    private long responseTime;
    private LocalDateTime timestamp;
    private String errorMessage;

    public AdminApiLogDto() {}

    public AdminApiLogDto(Long id, Long userId, String userEmail, String endpoint, 
                         String method, int statusCode, long responseTime, 
                         LocalDateTime timestamp, String errorMessage) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.endpoint = endpoint;
        this.method = method;
        this.statusCode = statusCode;
        this.responseTime = responseTime;
        this.timestamp = timestamp;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
} 
 
 