package com.dembasiby.user.controller;

import com.dembasiby.user.dto.ApiResponse;
import com.dembasiby.user.dto.JwtResponseDto;
import com.dembasiby.user.dto.LoginRequestDto;
import com.dembasiby.user.dto.UserRegistrationDto;
import com.dembasiby.user.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private UserRegistrationDto userRegistrationDto;
    private LoginRequestDto loginRequestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test data
        userRegistrationDto = new UserRegistrationDto();
        userRegistrationDto.setEmail("test@example.com");
        userRegistrationDto.setPassword("password123");
        
        loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("test@example.com");
        loginRequestDto.setPassword("password123");
    }

    @Test
    void register_Success() {
        // Arrange
        ApiResponse<String> expectedResponse = new ApiResponse<>(true, "User registered successfully");
        when(authService.register(any(UserRegistrationDto.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<String>> responseEntity = authController.register(userRegistrationDto);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(authService, times(1)).register(userRegistrationDto);
    }

    @Test
    void register_IllegalArgumentException() {
        // Arrange
        String errorMessage = "Email is already in use";
        when(authService.register(any(UserRegistrationDto.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // Act
        ResponseEntity<ApiResponse<String>> responseEntity = authController.register(userRegistrationDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertFalse(responseEntity.getBody().isSuccess());
        assertEquals(errorMessage, responseEntity.getBody().getMessage());
    }

    @Test
    void register_GenericException() {
        // Arrange
        when(authService.register(any(UserRegistrationDto.class)))
                .thenThrow(new RuntimeException("Some error"));

        // Act
        ResponseEntity<ApiResponse<String>> responseEntity = authController.register(userRegistrationDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertFalse(responseEntity.getBody().isSuccess());
        assertEquals("An unexpected error occurred", responseEntity.getBody().getMessage());
    }

    @Test
    void login_Success() {
        // Arrange
        List<String> roles = Arrays.asList("ROLE_USER");
        JwtResponseDto jwtResponse = new JwtResponseDto("token123", "test@example.com", roles);
        when(authService.login(any(LoginRequestDto.class))).thenReturn(jwtResponse);

        // Act
        ResponseEntity<?> responseEntity = authController.login(loginRequestDto);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<?> apiResponse = (ApiResponse<?>) responseEntity.getBody();
        assertTrue(apiResponse.isSuccess());
        assertEquals("Login successful", apiResponse.getMessage());
        assertEquals(jwtResponse, apiResponse.getData());
    }

    @Test
    void login_ResponseStatusException() {
        // Arrange
        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        // Act
        ResponseEntity<?> responseEntity = authController.login(loginRequestDto);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        ApiResponse<?> apiResponse = (ApiResponse<?>) responseEntity.getBody();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Invalid credentials", apiResponse.getMessage());
    }

    @Test
    void login_IllegalArgumentException() {
        // Arrange
        String errorMessage = "User not found";
        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // Act
        ResponseEntity<?> responseEntity = authController.login(loginRequestDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ApiResponse<?> apiResponse = (ApiResponse<?>) responseEntity.getBody();
        assertFalse(apiResponse.isSuccess());
        assertEquals(errorMessage, apiResponse.getMessage());
    }

    @Test
    void registerAdmin_Success() {
        // Arrange
        ApiResponse<String> expectedResponse = new ApiResponse<>(true, "Admin user registered successfully");
        when(authService.registerAdmin(any(UserRegistrationDto.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<String>> responseEntity = authController.registerAdmin(userRegistrationDto);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(authService, times(1)).registerAdmin(userRegistrationDto);
    }

    @Test
    void promoteToAdmin_Success() {
        // Arrange
        String email = "user@example.com";
        ApiResponse<String> expectedResponse = new ApiResponse<>(true, "User promoted to admin successfully");
        when(authService.promoteToAdmin(email)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<String>> responseEntity = authController.promoteToAdmin(email);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(authService, times(1)).promoteToAdmin(email);
    }

    @Test
    void promoteToAdmin_AlreadyAdmin() {
        // Arrange
        String email = "admin@example.com";
        ApiResponse<String> expectedResponse = new ApiResponse<>(false, "User already has admin role");
        when(authService.promoteToAdmin(email)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<String>> responseEntity = authController.promoteToAdmin(email);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
    }
}