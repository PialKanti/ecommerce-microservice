package com.example.ecommerce.auth.service;

import com.example.ecommerce.auth.dto.request.ChangePasswordRequest;
import com.example.ecommerce.auth.dto.request.UpdateProfileRequest;
import com.example.ecommerce.auth.dto.response.UserResponse;

public interface UserProfileService {
    UserResponse getProfile(Long userId);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);
}
