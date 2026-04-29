package com.manoj.fastserve.Controller;

import com.manoj.fastserve.DTO.LoginResponse;
import com.manoj.fastserve.Entity.User;
import com.manoj.fastserve.Service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Signup
    @PostMapping("/signup")
    public User signup(@RequestBody User user) {
        return userService.register(user);
    }

    // Login
    @PostMapping("/login")
    public LoginResponse login(
            @RequestParam String email,
            @RequestParam String password) {

        return userService.login(email, password);
    }
}