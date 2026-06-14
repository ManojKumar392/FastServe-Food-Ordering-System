package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.LoginResponse;
import com.manoj.fastserve.DTO.UserResponseDTO;
import com.manoj.fastserve.Entity.Role;
import com.manoj.fastserve.Entity.User;
import com.manoj.fastserve.Exception.BadRequestException;
import com.manoj.fastserve.Exception.ResourceNotFoundException;
import com.manoj.fastserve.Repository.UserRepository;
import com.manoj.fastserve.Util.JwtUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {


    @Mock
    private UserRepository userRepository;


    @Mock
    private PasswordEncoder passwordEncoder;


    @Mock
    private JwtUtil jwtUtil;


    @Mock
    private RefreshTokenService refreshTokenService;


    @InjectMocks
    private UserService userService;



    @Test
    void register_shouldCreateUserSuccessfully() {


        User user = new User();

        user.setName("Manoj");
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setAddress("India");


        when(passwordEncoder.encode("password"))
                .thenReturn("encrypted");


        when(userRepository.save(user))
                .thenReturn(user);



        UserResponseDTO response =
                userService.register(user);



        assertEquals("Manoj", response.getName());
        assertEquals("test@test.com", response.getEmail());


        assertEquals(Role.USER, user.getRole());

        verify(passwordEncoder)
                .encode("password");


        verify(userRepository)
                .save(user);

    }



    @Test
    void login_shouldReturnTokenWhenCredentialsCorrect() {


        User user = new User();

        user.setName("Manoj");
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPassword("encrypted");
        user.setRole(Role.USER);



        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));


        when(passwordEncoder.matches(
                "password",
                "encrypted"
        ))
                .thenReturn(true);



        when(jwtUtil.generateAccessToken(
                "test@test.com",
                "USER"
        ))
                .thenReturn("access-token");


        when(jwtUtil.generateRefreshToken(
                "test@test.com"
        ))
                .thenReturn("refresh-token");



        LoginResponse response =
                userService.login(
                        "test@test.com",
                        "password"
                );



        assertEquals(
                "access-token",
                response.getAccessToken()
        );


        verify(refreshTokenService)
                .createRefreshToken(
                        user,
                        "refresh-token"
                );

    }



    @Test
    void login_shouldThrowExceptionWhenUserNotFound() {


        when(userRepository.findByEmail("abc@test.com"))
                .thenReturn(Optional.empty());



        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.login(
                        "abc@test.com",
                        "password"
                )
        );

    }




    @Test
    void login_shouldThrowExceptionWhenPasswordWrong() {


        User user = new User();

        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPassword("encrypted");



        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));



        when(passwordEncoder.matches(
                "wrong",
                "encrypted"
        ))
                .thenReturn(false);



        assertThrows(
                BadRequestException.class,
                () -> userService.login(
                        "test@test.com",
                        "wrong"
                )
        );

    }



}


