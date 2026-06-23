package com.example.ecommerce.auth.service.impl;

import com.example.ecommerce.auth.dto.request.ChangePasswordRequest;
import com.example.ecommerce.auth.dto.request.UpdateProfileRequest;
import com.example.ecommerce.auth.dto.response.UserResponse;
import com.example.ecommerce.auth.entity.User;
import com.example.ecommerce.auth.exception.BadRequestException;
import com.example.ecommerce.auth.mapper.UserMapper;
import com.example.ecommerce.auth.repository.UserRepository;
import com.example.ecommerce.auth.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserMapper     userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        user.setModifiedBy(userId);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException("New password must differ from current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setModifiedBy(userId);
        userRepository.save(user);
    }
}
