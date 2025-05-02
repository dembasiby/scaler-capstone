package com.dembasiby.user.controller;

import com.dembasiby.user.dto.ApiResponse;
import com.dembasiby.user.dto.UserProfileDto;
import com.dembasiby.user.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserProfileDto>>> getAllUsers() {
        List<UserProfileDto> profiles = userProfileService.getAllUserProfiles();
        return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved successfully", profiles));
    }
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        UserProfileDto profile = userProfileService.getCurrentUserProfile(email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile retrieved successfully", profile));
    }
    
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateUserProfile(@RequestBody UserProfileDto profileDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        try {
            ApiResponse<UserProfileDto> response = userProfileService.updateUserProfile(email, profileDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage()));
        }
    }
}
