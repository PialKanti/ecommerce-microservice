package com.example.ecommerce.product.dto.response;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        String name,
        String code,
        String description,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        Long createdBy,
        Long modifiedBy
) {
}
