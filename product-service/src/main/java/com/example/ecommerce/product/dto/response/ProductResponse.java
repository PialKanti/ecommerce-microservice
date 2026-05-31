package com.example.ecommerce.product.dto.response;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        Double price,
        Boolean isActive,
        Long categoryId,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        Long createdBy,
        Long modifiedBy
) {
}
