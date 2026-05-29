package com.example.ecommerce.auth.dto.response;

import java.time.LocalDateTime;

public record PermissionResponse(
        Long id,
        String name,
        String code,
        String description,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        Long createdBy,
        Long modifiedBy
) {
}
