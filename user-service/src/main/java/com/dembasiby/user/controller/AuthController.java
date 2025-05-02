package com.dembasiby.user.controller;

import com.dembasiby.user.dto.LoginRequestDto;
import com.dembasiby.user.dto.UserRegistrationDto;
import com.dembasiby.user.dto.ApiResponse;
import com.dembasiby.user.dto.JwtResponseDto;
import com.dembasiby.user.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService userService) {
        this.authService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody UserRegistrationDto userRegistrationDto) {
        try {
            ApiResponse<String> response = authService.register(userRegistrationDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "An unexpected error occurred"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        try {
            JwtResponseDto jwtResponse = authService.login(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", jwtResponse));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(false, e.getReason()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "An unexpected error occurred"));
        }
    }

}
