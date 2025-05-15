package com.dembasiby.user.controller;

import com.dembasiby.user.dto.ApiResponse;
import com.dembasiby.user.dto.JwtResponseDto;
import com.dembasiby.user.dto.LoginRequestDto;
import com.dembasiby.user.dto.UserRegistrationDto;
import com.dembasiby.user.security.JwtAuthenticationFilter;
import com.dembasiby.user.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;
    
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void register_Success() throws Exception {
        // Arrange
        UserRegistrationDto userDto = new UserRegistrationDto();
        userDto.setEmail("test@example.com");
        userDto.setPassword("password123");
        
        ApiResponse<String> response = new ApiResponse<>(true, "User registered successfully");
        when(authService.register(any(UserRegistrationDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void login_Success() throws Exception {
        // Arrange
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");
        
        JwtResponseDto jwtResponse = new JwtResponseDto(
                "eyJhbGciOiJIUzI1NiJ9...", 
                "test@example.com", 
                Arrays.asList("ROLE_USER")
        );
        
        when(authService.login(any(LoginRequestDto.class))).thenReturn(jwtResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("eyJhbGciOiJIUzI1NiJ9..."))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        // Arrange
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("wrongpassword");
        
        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerAdmin_Success() throws Exception {
        // Arrange
        UserRegistrationDto userDto = new UserRegistrationDto();
        userDto.setEmail("admin@example.com");
        userDto.setPassword("adminpass123");
        
        ApiResponse<String> response = new ApiResponse<>(true, "Admin user registered successfully");
        when(authService.registerAdmin(any(UserRegistrationDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register-admin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Admin user registered successfully"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void registerAdmin_Forbidden() throws Exception {
        // Arrange
        UserRegistrationDto userDto = new UserRegistrationDto();
        userDto.setEmail("admin@example.com");
        userDto.setPassword("adminpass123");

        // Act & Assert - should be forbidden for non-admin users
        mockMvc.perform(post("/api/auth/register-admin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void promoteToAdmin_Success() throws Exception {
        // Arrange
        String email = "user@example.com";
        ApiResponse<String> response = new ApiResponse<>(true, "User promoted to admin successfully");
        when(authService.promoteToAdmin(eq(email))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/auth/promote/{email}", email)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User promoted to admin successfully"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void promoteToAdmin_Forbidden() throws Exception {
        // Arrange
        String email = "user@example.com";

        // Act & Assert - should be forbidden for non-admin users
        mockMvc.perform(put("/api/auth/promote/{email}", email)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void promoteToAdmin_AlreadyAdmin() throws Exception {
        // Arrange
        String email = "admin@example.com";
        ApiResponse<String> response = new ApiResponse<>(false, "User already has admin role");
        when(authService.promoteToAdmin(eq(email))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/auth/promote/{email}", email)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User already has admin role"));
    }
}