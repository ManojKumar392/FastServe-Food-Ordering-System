package com.manoj.fastserve.DTO;

public class LoginResponse {

    private String message;
    private Long userId;
    private String name;
    private String email;
    private String token;

    public LoginResponse(String message, Long userId, String name, String email, String token) {
        this.message = message;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}