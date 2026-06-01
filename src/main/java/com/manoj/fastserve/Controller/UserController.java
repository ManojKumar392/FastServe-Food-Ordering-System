package com.manoj.fastserve.Controller;

import com.manoj.fastserve.DTO.*;
import com.manoj.fastserve.Entity.User;
import com.manoj.fastserve.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Signup
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> signup(@Valid @RequestBody User user) {
        return new ResponseEntity<>(
                userService.register(user),
                HttpStatus.CREATED
        );
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestParam String email,
            @RequestParam String password) {

        return ResponseEntity.ok(
                userService.login(email, password)
        );
    }

    // Refresh
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refreshToken(
            @RequestBody RefreshTokenRequest request
    ) {

        return ResponseEntity.ok(
                userService.refreshAccessToken(
                        request.getRefreshToken()
                )
        );
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestBody LogoutRequest request
    ) {

        userService.logout(
                request.getRefreshToken()
        );

        return ResponseEntity.ok(
                "Logout successful"
        );
    }
}