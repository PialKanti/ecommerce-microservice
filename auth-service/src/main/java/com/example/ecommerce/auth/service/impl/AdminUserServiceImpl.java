package com.example.ecommerce.auth.service.impl;

import com.example.ecommerce.auth.dto.request.UpdateUserStatusRequest;
import com.example.ecommerce.auth.dto.response.UserResponse;
import com.example.ecommerce.auth.entity.User;
import com.example.ecommerce.auth.mapper.UserMapper;
import com.example.ecommerce.auth.repository.UserRepository;
import com.example.ecommerce.auth.service.AdminUserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserMapper     userMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(String search, Boolean isActive, Pageable pageable) {
        return userRepository.findAllWithFilters(search, isActive, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long userId) {
        User user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateStatus(Long userId, UpdateUserStatusRequest request, Long adminUserId) {
        User user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        user.setIsActive(request.isActive());
        user.setModifiedBy(adminUserId);
        return userMapper.toResponse(userRepository.save(user));
    }
}
