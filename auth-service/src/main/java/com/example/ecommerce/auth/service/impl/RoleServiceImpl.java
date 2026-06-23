package com.example.ecommerce.auth.service.impl;

import com.example.ecommerce.commons.exception.ResourceConflictException;
import com.example.ecommerce.auth.dto.request.RoleRequest;
import com.example.ecommerce.auth.dto.response.PermissionResponse;
import com.example.ecommerce.auth.dto.response.RoleResponse;
import com.example.ecommerce.auth.entity.Permission;
import com.example.ecommerce.auth.entity.Role;
import com.example.ecommerce.commons.enums.RoleCode;
import com.example.ecommerce.auth.repository.PermissionRepository;
import com.example.ecommerce.auth.repository.RoleRepository;
import com.example.ecommerce.auth.service.RoleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository       roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponse> getAll(Pageable pageable) {
        return roleRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getById(Long id) {
        return toResponse(getRoleWithPermissions(id));
    }

    @Override
    @Transactional
    public RoleResponse create(RoleRequest request) {
        RoleCode code = request.code();
        if (roleRepository.existsByCode(code)) {
            throw new ResourceConflictException("Role with code '" + code + "' already exists.");
        }
        Role role = Role.builder()
                .name(request.name())
                .code(code)
                .description(request.description())
                .build();
        return toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse update(Long id, RoleRequest request) {
        Role role = getRoleWithPermissions(id);
        RoleCode code = request.code();
        roleRepository.findByCode(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResourceConflictException("Role with code '" + code + "' already exists.");
                });
        role.setName(request.name());
        role.setCode(code);
        role.setDescription(request.description());
        return toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new EntityNotFoundException("Role not found: " + id);
        }
        roleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissions(Long roleId) {
        return getRoleWithPermissions(roleId).getPermissions()
                .stream()
                .sorted(Comparator.comparing(Permission::getCode))
                .map(this::toPermissionResponse)
                .toList();
    }

    @Override
    @Transactional
    public RoleResponse assignPermission(Long roleId, Long permissionId) {
        Role role = getRoleWithPermissions(roleId);
        Permission permission = getPermission(permissionId);
        role.getPermissions().add(permission);
        return toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse removePermission(Long roleId, Long permissionId) {
        Role role = getRoleWithPermissions(roleId);
        Permission permission = getPermission(permissionId);
        role.getPermissions().removeIf(existing -> existing.getId().equals(permission.getId()));
        return toResponse(roleRepository.save(role));
    }

    private Role getRoleWithPermissions(Long id) {
        return roleRepository.findWithPermissionsById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + id));
    }

    private Permission getPermission(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + id));
    }

    private RoleResponse toResponse(Role role) {
        Set<String> permissions = role.getPermissions()
                .stream()
                .map(Permission::getCode)
                .map(Enum::name)
                .collect(Collectors.toCollection(java.util.TreeSet::new));
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .code(role.getCode().name())
                .description(role.getDescription())
                .permissions(permissions)
                .createdAt(role.getCreatedAt())
                .modifiedAt(role.getModifiedAt())
                .createdBy(role.getCreatedBy())
                .modifiedBy(role.getModifiedBy())
                .build();
    }

    private PermissionResponse toPermissionResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .code(permission.getCode().name())
                .description(permission.getDescription())
                .createdAt(permission.getCreatedAt())
                .modifiedAt(permission.getModifiedAt())
                .createdBy(permission.getCreatedBy())
                .modifiedBy(permission.getModifiedBy())
                .build();
    }
}
