package com.dembasiby.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SocialLoginRequestDto {
    @NotBlank(message = "Provider is required")
    private String provider; // "google", "facebook", etc.
    
    @NotBlank(message = "Token is required")
    private String token;
    
    @Email(message = "Email should be valid")
    private String email;
    
    private String name;
    
    // Getters and setters
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}