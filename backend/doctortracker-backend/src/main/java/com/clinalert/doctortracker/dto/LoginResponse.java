package com.clinalert.doctortracker.dto;

public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private String userId;
    private String email;
    private String role;

    public LoginResponse() {
    }

    public LoginResponse(String token, String userId, String email, String role) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public LoginResponse(String token, String type, String userId, String email, String role) {
        this.token = token;
        this.type = type;
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    // Getters - Required for JSON serialization
    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    // Setters
    public void setToken(String token) {
        this.token = token;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
