package com.example.ecommerce.auth.service;

import com.example.ecommerce.auth.dto.request.PermissionRequest;
import com.example.ecommerce.auth.dto.response.PermissionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PermissionService {
    Page<PermissionResponse> getAll(Pageable pageable);
    PermissionResponse getById(Long id);
    PermissionResponse create(PermissionRequest request);
    PermissionResponse update(Long id, PermissionRequest request);
    void delete(Long id);
}
