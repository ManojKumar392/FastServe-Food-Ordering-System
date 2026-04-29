package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.LoginResponse;
import com.manoj.fastserve.Entity.User;
import com.manoj.fastserve.Repository.UserRepository;
import org.springframework.stereotype.Service;

import com.manoj.fastserve.Repository.UserRepository;
import com.manoj.fastserve.Entity.User;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Signup
    public User register(User user) {
        return userRepository.save(user);
    }

    // Login
    public LoginResponse login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }

        return new LoginResponse(
                "Login successful",
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}