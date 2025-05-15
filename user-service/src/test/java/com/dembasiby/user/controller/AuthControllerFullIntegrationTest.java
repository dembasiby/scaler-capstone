package com.dembasiby.user.controller;

import com.dembasiby.user.dto.LoginRequestDto;
import com.dembasiby.user.dto.UserRegistrationDto;
import com.dembasiby.user.entity.Authority;
import com.dembasiby.user.entity.User;
import com.dembasiby.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerFullIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        // Create an admin user
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("adminpass"));
        adminUser.setAuthorities(Collections.singletonList(new Authority("ROLE_ADMIN")));
        userRepository.save(adminUser);

        // Create a regular user
        User regularUser = new User();
        regularUser.setEmail("user@example.com");
        regularUser.setPassword(passwordEncoder.encode("userpass"));
        regularUser.setAuthorities(Collections.singletonList(new Authority("ROLE_USER")));
        userRepository.save(regularUser);

        // Login as admin to get token
        LoginRequestDto adminLoginDto = new LoginRequestDto();
        adminLoginDto.setEmail("admin@example.com");
        adminLoginDto.setPassword("adminpass");

        MvcResult adminResult = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLoginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String adminResponse = adminResult.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(adminResponse)
                .path("data")
                .path("token")
                .asText();

        // Login as regular user to get token
        LoginRequestDto userLoginDto = new LoginRequestDto();
        userLoginDto.setEmail("user@example.com");
        userLoginDto.setPassword("userpass");

        MvcResult userResult = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String userResponse = userResult.getResponse().getContentAsString();
        userToken = objectMapper.readTree(userResponse)
                .path("data")
                .path("token")
                .asText();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void register_Success() throws Exception {
        // Arrange
        UserRegistrationDto userDto = new UserRegistrationDto();
        userDto.setEmail("newuser@example.com");
        userDto.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        // Verify user was created in database
        assertTrue(userRepository.findByEmail("newuser@example.com").isPresent());
    }

    @Test
    void registerAdmin_Success() throws Exception {
        // Arrange
        UserRegistrationDto adminDto = new UserRegistrationDto();
        adminDto.setEmail("newadmin@example.com");
        adminDto.setPassword("adminpass123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register-admin")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Admin user registered successfully"));

        // Verify admin was created in database
        User newAdmin = userRepository.findByEmail("newadmin@example.com").orElse(null);
        assertNotNull(newAdmin);
        assertTrue(newAdmin.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void registerAdmin_Forbidden_ForRegularUser() throws Exception {
        // Arrange
        UserRegistrationDto adminDto = new UserRegistrationDto();
        adminDto.setEmail("newadmin2@example.com");
        adminDto.setPassword("adminpass123");

        // Act & Assert - regular user can't create admin
        mockMvc.perform(post("/api/auth/register-admin")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void promoteToAdmin_Success() throws Exception {
        // Arrange - create a new user to promote
        UserRegistrationDto userDto = new UserRegistrationDto();
        userDto.setEmail("promote@example.com");
        userDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        // Act & Assert - promote the user to admin
        mockMvc.perform(put("/api/auth/promote/{email}", "promote@example.com")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User promoted to admin successfully"));

        // Verify user now has admin role
        User promotedUser = userRepository.findByEmail("promote@example.com").orElse(null);
        assertNotNull(promotedUser);
        assertTrue(promotedUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void promoteToAdmin_Forbidden_ForRegularUser() throws Exception {
        // Act & Assert - regular user can't promote to admin
        mockMvc.perform(put("/api/auth/promote/{email}", "user@example.com")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}