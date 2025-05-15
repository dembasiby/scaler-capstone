package com.dembasiby.user.service;

import com.dembasiby.user.dto.ApiResponse;
import com.dembasiby.user.dto.JwtResponseDto;
import com.dembasiby.user.dto.UserRegistrationDto;
import com.dembasiby.user.dto.LoginRequestDto;
import com.dembasiby.user.entity.Authority;
import com.dembasiby.user.entity.User;
import com.dembasiby.user.repository.UserRepository;
import com.dembasiby.user.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public ApiResponse<String> register(UserRegistrationDto userDto) {
        String email = userDto.getEmail();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setAuthorities(Collections.singletonList(new Authority("ROLE_USER")));

        userRepository.save(user);

        return new ApiResponse<>(true, "User registered successfully");
    }

    public JwtResponseDto login(LoginRequestDto requestDto) {
        try {
            // Attempt authentication
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getEmail(),
                            requestDto.getPassword()
                    )
            );
            
            User user = userRepository.findByEmail(requestDto.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            String token = jwtUtil.generateToken(user);
            
            List<String> roles = user.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(Collectors.toList());
                    
            return new JwtResponseDto(token, user.getEmail(), roles);
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,  // Return 401 instead of 403
                    "Invalid email or password"
            );
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "An error occurred during login: " + e.getMessage()
            );
        }
    }

    public ApiResponse<String> registerAdmin(UserRegistrationDto userDto) {
        String email = userDto.getEmail();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setAuthorities(Collections.singletonList(new Authority("ROLE_ADMIN")));

        userRepository.save(user);

        return new ApiResponse<>(true, "Admin user registered successfully");
    }

    public ApiResponse<String> promoteToAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if user already has admin role
        boolean isAlreadyAdmin = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAlreadyAdmin) {
            return new ApiResponse<>(false, "User already has admin role");
        }
        
        // Get the current authorities and create a new list with the admin role added
        List<Authority> currentAuthorities = new ArrayList<>(user.getAuthorities().size() + 1);
        currentAuthorities.addAll((Collection<Authority>) user.getAuthorities());
        currentAuthorities.add(new Authority("ROLE_ADMIN"));
        
        // Set the updated authorities list
        user.setAuthorities(currentAuthorities);
        userRepository.save(user);
        
        return new ApiResponse<>(true, "User promoted to admin successfully");
    }
}
