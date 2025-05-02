package com.dembasiby.user.dto;

import java.util.List;

public class JwtResponseDto {
    private String token;
    private String tokenType = "Bearer";
    private String email;
    private List<String> roles;

    public JwtResponseDto(String token, String email, List<String> roles) {
        this.token = token;
        this.email = email;
        this.roles = roles;
    }

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}