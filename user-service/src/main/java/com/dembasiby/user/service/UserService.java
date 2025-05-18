package com.dembasiby.user.service;

import com.dembasiby.user.entity.User;
import com.dembasiby.user.entity.Authority;
import com.dembasiby.user.exception.ResourceNotFoundException;
import com.dembasiby.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User createUser(User user) {
        // Ensure new users get a default USER authority if none specified
        if (user.getAuthorities() == null || user.getAuthorities().isEmpty()) {
            List<Authority> authorities = new ArrayList<>();
            authorities.add(new Authority("ROLE_USER"));
            user.setAuthorities(authorities);
        }
        return userRepository.save(user);
    }
    
    public User createAdminUser(User user) {
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new Authority("ROLE_ADMIN"));
        user.setAuthorities(authorities);
        return userRepository.save(user);
    }
    
    public User promoteToAdmin(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Create a new list from the existing authorities
        List<Authority> authorities = new ArrayList<>(
            user.getAuthorities().stream()
                .filter(auth -> auth instanceof Authority)
                .map(auth -> (Authority) auth)
                .collect(Collectors.toList())
        );
        
        // Add the admin authority
        authorities.add(new Authority("ROLE_ADMIN"));
        user.setAuthorities(authorities);
        
        return userRepository.save(user);
    }
}