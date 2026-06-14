package com.manoj.fastserve.DTO;

public class LoginResponse {

    private String message;
    private Long userId;
    private String name;
    private String email;

    private String accessToken;
    private String refreshToken;

    public LoginResponse(
            String message,
            Long userId,
            String name,
            String email,
            String accessToken,
            String refreshToken
    ) {
        this.message = message;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
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

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

