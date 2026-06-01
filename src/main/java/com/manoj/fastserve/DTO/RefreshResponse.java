package com.manoj.fastserve.DTO;

import com.manoj.fastserve.Entity.RefreshToken;
import com.manoj.fastserve.Repository.RefreshTokenRepository;

public class RefreshResponse {

    private String accessToken;
    private String refreshToken;

    public RefreshResponse(
            String accessToken,
            String refreshToken
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}