package com.manoj.fastserve.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manoj.fastserve.Config.TestCacheConfig;
import com.manoj.fastserve.DTO.LoginResponse;
import com.manoj.fastserve.DTO.RefreshResponse;
import com.manoj.fastserve.DTO.SignupRequest;
import com.manoj.fastserve.DTO.UserResponseDTO;
import com.manoj.fastserve.Entity.User;

import com.manoj.fastserve.Service.UserService;

import com.manoj.fastserve.Util.JwtUtil;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Import(TestCacheConfig.class)
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {


    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void signup_shouldCreateUser() throws Exception {


        UserResponseDTO response =
                new UserResponseDTO();


        response.setId(1L);
        response.setName("Manoj");
        response.setEmail("test@gmail.com");


        when(userService.register(any(SignupRequest.class)))
                .thenReturn(response);



        mockMvc.perform(
                        post("/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                          "name":"Manoj",
                          "email":"test@gmail.com",
                          "password":"password123",
                          "address":"India"
                        }
                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email")
                        .value("test@gmail.com"));


    }




    @Test
    void login_shouldReturnTokens() throws Exception {


        LoginResponse response =
                new LoginResponse(
                        "Login successful",
                        1L,
                        "Manoj",
                        "test@gmail.com",
                        "access123",
                        "refresh123"
                );


        when(userService.login(
                "test@gmail.com",
                "password123"
        ))
                .thenReturn(response);



        mockMvc.perform(
                        post("/users/login")
                                .param("email","test@gmail.com")
                                .param("password","password123")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("access123"))
                .andExpect(jsonPath("$.refreshToken")
                        .value("refresh123"));

    }




    @Test
    void refresh_shouldReturnNewTokens() throws Exception {


        RefreshResponse response =
                new RefreshResponse(
                        "newAccess",
                        "newRefresh"
                );


        when(userService.refreshAccessToken(
                "oldRefresh"
        ))
                .thenReturn(response);



        mockMvc.perform(
                        post("/users/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                          "refreshToken":"oldRefresh"
                        }
                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("newAccess"));

    }



    @Test
    void logout_shouldReturnSuccess() throws Exception {


        doNothing()
                .when(userService)
                .logout("refresh123");



        mockMvc.perform(
                        post("/users/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                          "refreshToken":"refresh123"
                        }
                        """)
                )
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("Logout successful"));

    }

}