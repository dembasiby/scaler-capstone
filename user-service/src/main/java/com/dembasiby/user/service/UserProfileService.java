package com.dembasiby.user.service;

import com.dembasiby.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {
    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
