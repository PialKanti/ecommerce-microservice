package com.example.ecommerce.inventory.dto.response;

import java.time.LocalDateTime;

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
