package org.example.dto.admin;

public class AdminApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    
    public AdminApiResponse() {}
    
    public AdminApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    // Геттеры и сеттеры
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
} 
 
 