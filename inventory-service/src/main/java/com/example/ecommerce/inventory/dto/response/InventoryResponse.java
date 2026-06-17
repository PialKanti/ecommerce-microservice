package com.example.ecommerce.inventory.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InventoryResponse(
        Long id,
        Long productId,
        Integer totalQuantity,
        Integer reservedQuantity,
        Integer availableQuantity,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        Long createdBy,
        Long modifiedBy
) {
}
