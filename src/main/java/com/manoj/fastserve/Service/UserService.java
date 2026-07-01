package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.LoginResponse;
import com.manoj.fastserve.DTO.RefreshResponse;
import com.manoj.fastserve.DTO.UserResponseDTO;
import com.manoj.fastserve.Entity.RefreshToken;
import com.manoj.fastserve.Entity.User;
import com.manoj.fastserve.Entity.Role;
import com.manoj.fastserve.Exception.BadRequestException;
import com.manoj.fastserve.Exception.ResourceNotFoundException;
import com.manoj.fastserve.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.manoj.fastserve.Util.JwtUtil;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    // Signup
    public UserResponseDTO register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new BadRequestException("Email already registered");
        }
        User saved = userRepository.save(user);
        return mapToDTO(saved);
    }

    // Login
    public LoginResponse login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid password");
        }

        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRole().name()
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                user.getEmail()
        );

        refreshTokenService.createRefreshToken(
                user,
                refreshToken
        );

        return new LoginResponse(
                "Login successful",
                user.getId(),
                user.getName(),
                user.getEmail(),
                accessToken,
                refreshToken
        );
    }

    private UserResponseDTO mapToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setAddress(user.getAddress());
        return dto;
    }

    public RefreshResponse refreshAccessToken(
            String refreshTokenValue
    ) {

        RefreshToken oldRefreshToken =
                refreshTokenService.verifyRefreshToken(
                        refreshTokenValue
                );

        User user = oldRefreshToken.getUser();

        String newAccessToken =
                jwtUtil.generateAccessToken(
                        user.getEmail(),
                        user.getRole().name()
                );

        String newRefreshToken =
                jwtUtil.generateRefreshToken(
                        user.getEmail()
                );

        refreshTokenService.deleteRefreshToken(
                oldRefreshToken
        );

        refreshTokenService.createRefreshToken(
                user,
                newRefreshToken
        );

        return new RefreshResponse(
                newAccessToken,
                newRefreshToken
        );
    }

    public void logout(String refreshToken) {

        refreshTokenService.deleteRefreshToken(refreshToken);
    }
}

