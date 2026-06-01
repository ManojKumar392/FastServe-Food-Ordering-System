package com.manoj.fastserve.Service;

import com.manoj.fastserve.Entity.RefreshToken;
import com.manoj.fastserve.Entity.User;
import com.manoj.fastserve.Exception.BadRequestException;
import com.manoj.fastserve.Repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(User user, String token) {

        refreshTokenRepository.findByUser(user)
                .ifPresent(existing -> refreshTokenRepository.delete(existing));

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setToken(token);

        refreshToken.setExpiryDate(
                LocalDateTime.now().plusDays(7)
        );

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() ->
                        new BadRequestException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {

            refreshTokenRepository.delete(refreshToken);

            throw new BadRequestException("Refresh token expired");
        }

        return refreshToken;
    }

    public void deleteRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() ->
                        new BadRequestException("Refresh token not found"));

        refreshTokenRepository.delete(refreshToken);
    }
}