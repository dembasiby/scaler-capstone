package com.dembasiby.user.controller;

import com.dembasiby.user.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserProfileService userProfileService;

    public UserController(UserProfileService userService) {
        this.userProfileService = userService;
    }

    @GetMapping
    public ResponseEntity<String> getAllUsers() {
        return ResponseEntity.ok("Hello world");
    }
}
