package com.dembasiby.user.service;

import com.dembasiby.user.dto.ApiResponse;
import com.dembasiby.user.dto.UserProfileDto;
import com.dembasiby.user.entity.Address;
import com.dembasiby.user.entity.User;
import com.dembasiby.user.entity.UserProfile;
import com.dembasiby.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserProfileService {
    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public UserProfileDto getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            return new UserProfileDto();
        }
        
        return mapToDto(profile);
    }
    
    @Transactional
    public ApiResponse<UserProfileDto> updateUserProfile(String email, UserProfileDto profileDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        UserProfile userProfile = user.getUserProfile();
        if (userProfile == null) {
            userProfile = new UserProfile();
            user.setUserProfile(userProfile);
        }

        // Update profile fields
        userProfile.setFirstName(profileDto.getFirstName());
        userProfile.setLastName(profileDto.getLastName());
        
        // Handle addresses
        if (profileDto.getAddresses() != null) {
            if (userProfile.getAddresses() == null) {
                userProfile.setAddresses(new HashSet<>());
            } else {
                userProfile.getAddresses().clear();
            }
            
            for (UserProfileDto.AddressDto addrDto : profileDto.getAddresses()) {
                Address address = new Address(
                    addrDto.getStreet(),
                    addrDto.getStreet2(),
                    addrDto.getCity(),
                    addrDto.getState(),
                    addrDto.getZip(),
                    addrDto.getCountry()
                );
                userProfile.getAddresses().add(address);
            }
        }
        
        userRepository.save(user);
        
        return new ApiResponse<>(true, "Profile updated successfully", mapToDto(userProfile));
    }
    
    public List<UserProfileDto> getAllUserProfiles() {
        return userRepository.findAll().stream()
                .map(User::getUserProfile)
                .filter(Objects::nonNull)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    private UserProfileDto mapToDto(UserProfile profile) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(profile.getId());
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        
        if (profile.getAddresses() != null) {
            dto.setAddresses(profile.getAddresses().stream()
                    .map(this::mapAddressToDto)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    private UserProfileDto.AddressDto mapAddressToDto(Address address) {
        UserProfileDto.AddressDto dto = new UserProfileDto.AddressDto();
        dto.setStreet(address.getStreet());
        dto.setStreet2(address.getStreet2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZip(address.getZip());
        dto.setCountry(address.getCountry());
        return dto;
    }
}