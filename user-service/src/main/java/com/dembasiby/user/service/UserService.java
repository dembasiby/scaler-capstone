package com.dembasiby.user.service;

import com.dembasiby.user.dto.UserRegistrationDto;
import com.dembasiby.user.dto.LoginRequestDto;
import com.dembasiby.user.entity.Authority;
import com.dembasiby.user.entity.User;
import com.dembasiby.user.repository.UserRepository;
import com.dembasiby.user.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public String registration(UserRegistrationDto userDto) {
        if (userRepository.findByUsername(userDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Username is already in use");
        }

        User user = new User();
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setAuthorities(Collections.singletonList(new Authority("ROLE_USER")));

        userRepository.save(user);

        return "User registered successfully";
    }

    public String login(LoginRequestDto requestDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.getEmail(),
                        requestDto.getPassword())
        );

        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return jwtUtil.generateToken(user);
    }
}
