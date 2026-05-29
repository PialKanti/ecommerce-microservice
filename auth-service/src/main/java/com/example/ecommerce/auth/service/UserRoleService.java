package com.example.ecommerce.auth.service;

import com.example.ecommerce.auth.dto.response.RoleResponse;

import java.util.List;

public interface UserRoleService {
    List<RoleResponse> getRoles(Long userId);
    List<RoleResponse> assignRole(Long userId, Long roleId);
    List<RoleResponse> removeRole(Long userId, Long roleId);
}
