package com.example.ecommerce.auth.service.impl;

import com.example.ecommerce.commons.exception.ResourceConflictException;
import com.example.ecommerce.auth.dto.request.PermissionRequest;
import com.example.ecommerce.auth.dto.response.PermissionResponse;
import com.example.ecommerce.auth.entity.Permission;
import com.example.ecommerce.commons.enums.PermissionCode;
import com.example.ecommerce.auth.repository.PermissionRepository;
import com.example.ecommerce.auth.service.PermissionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionResponse> getAll(Pageable pageable) {
        return permissionRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getById(Long id) {
        return toResponse(getPermission(id));
    }

    @Override
    @Transactional
    public PermissionResponse create(PermissionRequest request) {
        PermissionCode code = request.code();
        if (permissionRepository.existsByCode(code)) {
            throw new ResourceConflictException("Permission with code '" + code + "' already exists.");
        }
        Permission permission = new Permission();
        permission.setName(request.name());
        permission.setCode(code);
        permission.setDescription(request.description());
        return toResponse(permissionRepository.save(permission));
    }

    @Override
    @Transactional
    public PermissionResponse update(Long id, PermissionRequest request) {
        Permission permission = getPermission(id);
        PermissionCode code = request.code();
        permissionRepository.findByCode(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResourceConflictException("Permission with code '" + code + "' already exists.");
                });
        permission.setName(request.name());
        permission.setCode(code);
        permission.setDescription(request.description());
        return toResponse(permissionRepository.save(permission));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new EntityNotFoundException("Permission not found: " + id);
        }
        permissionRepository.deleteById(id);
    }

    private Permission getPermission(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + id));
    }

    private PermissionResponse toResponse(Permission permission) {
        return new PermissionResponse(
                permission.getId(), permission.getName(), permission.getCode().name(),
                permission.getDescription(), permission.getCreatedAt(), permission.getModifiedAt(),
                permission.getCreatedBy(), permission.getModifiedBy());
    }
}
