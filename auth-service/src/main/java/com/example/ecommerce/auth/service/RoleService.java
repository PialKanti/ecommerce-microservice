package com.example.ecommerce.auth.service;

import com.example.ecommerce.auth.dto.request.RoleRequest;
import com.example.ecommerce.auth.dto.response.PermissionResponse;
import com.example.ecommerce.auth.dto.response.RoleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {
    Page<RoleResponse> getAll(Pageable pageable);
    RoleResponse getById(Long id);
    RoleResponse create(RoleRequest request);
    RoleResponse update(Long id, RoleRequest request);
    void delete(Long id);
    List<PermissionResponse> getPermissions(Long roleId);
    RoleResponse assignPermission(Long roleId, Long permissionId);
    RoleResponse removePermission(Long roleId, Long permissionId);
}
