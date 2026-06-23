package com.example.ecommerce.auth.service;

import com.example.ecommerce.auth.dto.request.UpdateUserStatusRequest;
import com.example.ecommerce.auth.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<UserResponse> getAll(String search, Boolean isActive, Pageable pageable);

    UserResponse getById(Long userId);

    UserResponse updateStatus(Long userId, UpdateUserStatusRequest request, Long adminUserId);
}
