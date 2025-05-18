package com.dembasiby.user.service;

import com.dembasiby.user.dto.ApiResponse;
import com.dembasiby.user.dto.JwtResponseDto;
import com.dembasiby.user.dto.LoginRequestDto;
import com.dembasiby.user.dto.SocialLoginRequestDto;
import com.dembasiby.user.dto.UserRegistrationDto;
import com.dembasiby.user.entity.Authority;
import com.dembasiby.user.entity.User;
import com.dembasiby.user.entity.UserProfile;
import com.dembasiby.user.repository.UserRepository;
import com.dembasiby.user.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                      AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                      EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public JwtResponseDto login(LoginRequestDto loginDto) {
        logger.info("Attempting login for user: {}", loginDto.getEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );
            
            // Get the user from the repository instead of casting
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            String token = jwtUtil.generateToken(user);
            
            List<String> roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            
            logger.info("Login successful for user: {}", loginDto.getEmail());
            return new JwtResponseDto(token, loginDto.getEmail(), roles);
        } catch (Exception e) {
            logger.error("Login failed for user: {}", loginDto.getEmail(), e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
    }

    public ApiResponse<String> register(UserRegistrationDto userDto) {
        String email = userDto.getEmail();
        logger.info("Attempting to register user with email: {}", email);
        
        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("Registration failed: Email {} is already in use", email);
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setAuthorities(Collections.singletonList(new Authority("ROLE_USER")));

        userRepository.save(user);
        logger.info("User registered successfully: {}", email);

        return new ApiResponse<>(true, "User registered successfully");
    }
    
    public ApiResponse<String> registerAdmin(UserRegistrationDto userDto) {
        String email = userDto.getEmail();
        logger.info("Attempting to register admin with email: {}", email);
        
        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("Admin registration failed: Email {} is already in use", email);
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setAuthorities(Collections.singletonList(new Authority("ROLE_ADMIN")));

        userRepository.save(user);
        logger.info("Admin registered successfully: {}", email);

        return new ApiResponse<>(true, "Admin registered successfully");
    }
    
    public ApiResponse<String> promoteToAdmin(String email) {
        logger.info("Attempting to promote user to admin: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if already an admin
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
        
        if (isAdmin) {
            logger.info("User {} is already an admin", email);
            return new ApiResponse<>(true, "User is already an admin");
        }
        
        // Create a new list with all existing authorities plus the new one
        List<Authority> updatedAuthorities = new ArrayList<>();
        user.getAuthorities().forEach(auth -> {
            if (auth instanceof Authority) {
                updatedAuthorities.add((Authority) auth);
            }
        });
        updatedAuthorities.add(new Authority("ROLE_ADMIN"));
        
        // Set the updated authorities list
        user.setAuthorities(updatedAuthorities);
        userRepository.save(user);
        
        logger.info("User {} promoted to admin successfully", email);
        return new ApiResponse<>(true, "User promoted to admin successfully");
    }
    
    @Transactional
    public JwtResponseDto socialLogin(SocialLoginRequestDto socialLoginDto) {
        logger.info("Attempting social login for user: {} with provider: {}", 
                socialLoginDto.getEmail(), socialLoginDto.getProvider());
        
        // In a real implementation, you would validate the token with the provider
        // For simplicity, we'll assume the token is valid
        
        Optional<User> existingUser = userRepository.findByEmail(socialLoginDto.getEmail());
        User user;
        
        if (existingUser.isPresent()) {
            user = existingUser.get();
            logger.info("Existing user found for social login: {}", socialLoginDto.getEmail());
        } else {
            // Create a new user
            user = new User();
            user.setEmail(socialLoginDto.getEmail());
            // Generate a random password for social users
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            
            // Create authorities list
            List<Authority> authorities = new ArrayList<>();
            authorities.add(new Authority("ROLE_USER"));
            user.setAuthorities(authorities);
            
            // Create user profile if name is provided
            if (socialLoginDto.getName() != null && !socialLoginDto.getName().isEmpty()) {
                UserProfile profile = new UserProfile();
                String[] nameParts = socialLoginDto.getName().split(" ", 2);
                profile.setFirstName(nameParts[0]);
                if (nameParts.length > 1) {
                    profile.setLastName(nameParts[1]);
                }
                user.setUserProfile(profile);
            }
            
            userRepository.save(user);
            logger.info("New user created from social login: {}", socialLoginDto.getEmail());
        }
        
        String token = jwtUtil.generateToken(user);
        
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        return new JwtResponseDto(token, user.getEmail(), roles);
    }
    
    @Transactional
    public ApiResponse<String> initiatePasswordReset(String email) {
        logger.info("Initiating password reset for: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(System.currentTimeMillis() + 3600000); // 1 hour
        userRepository.save(user);
        
        // Send email with reset link
        String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;
        emailService.sendPasswordResetEmail(email, resetLink);
        
        logger.info("Password reset initiated for: {}", email);
        return new ApiResponse<>(true, "Password reset instructions sent to your email");
    }
    
    @Transactional
    public ApiResponse<String> resetPassword(String token, String newPassword) {
        logger.info("Resetting password with token");
        
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));
        
        // Check if token is expired
        if (user.getResetTokenExpiry() < System.currentTimeMillis()) {
            logger.warn("Password reset failed: Token expired");
            throw new IllegalArgumentException("Token has expired");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        
        logger.info("Password reset successful for user: {}", user.getEmail());
        return new ApiResponse<>(true, "Password has been reset successfully");
    }
}
