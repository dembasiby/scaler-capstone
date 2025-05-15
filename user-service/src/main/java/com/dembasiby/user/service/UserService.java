package com.dembasiby.user.service;
import com.dembasiby.user.entity.User;

public User createAdminUser(UserDto userDto) {
    User user = createUser(userDto);
    user.setRole(Role.ADMIN);
    return userRepository.save(user);
}

// Alternative: method to promote existing user to admin
public User promoteToAdmin(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setRole(Role.ADMIN);
    return userRepository.save(user);
}