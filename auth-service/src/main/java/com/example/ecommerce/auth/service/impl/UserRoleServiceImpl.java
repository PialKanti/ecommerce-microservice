package com.example.ecommerce.auth.service.impl;

import com.example.ecommerce.auth.dto.response.RoleResponse;
import com.example.ecommerce.auth.entity.Permission;
import com.example.ecommerce.auth.entity.Role;
import com.example.ecommerce.auth.entity.User;
import com.example.ecommerce.auth.repository.RoleRepository;
import com.example.ecommerce.auth.repository.UserRepository;
import com.example.ecommerce.auth.service.UserRoleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles(Long userId) {
        return getUserWithRoles(userId).getRoles()
                .stream()
                .sorted(Comparator.comparing(Role::getCode))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<RoleResponse> assignRole(Long userId, Long roleId) {
        User user = getUserWithRoles(userId);
        Role role = getRole(roleId);
        user.getRoles().add(role);
        return userRepository.save(user).getRoles()
                .stream()
                .sorted(Comparator.comparing(Role::getCode))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<RoleResponse> removeRole(Long userId, Long roleId) {
        User user = getUserWithRoles(userId);
        getRole(roleId);
        user.getRoles().removeIf(role -> role.getId().equals(roleId));
        return userRepository.save(user).getRoles()
                .stream()
                .sorted(Comparator.comparing(Role::getCode))
                .map(this::toResponse)
                .toList();
    }

    private User getUserWithRoles(Long userId) {
        return userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private Role getRole(Long roleId) {
        return roleRepository.findWithPermissionsById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleId));
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
}
