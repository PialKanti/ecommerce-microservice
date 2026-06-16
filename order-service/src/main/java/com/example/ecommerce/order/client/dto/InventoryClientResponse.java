package com.example.ecommerce.order.client.dto;

public record InventoryClientResponse(
        Long id,
        Long productId,
        Integer totalQuantity,
        Integer reservedQuantity,
        Integer availableQuantity
) {
}
