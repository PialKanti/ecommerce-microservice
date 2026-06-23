package com.example.ecommerce.cart.client.dto;

public record InventoryClientResponse(
        Long productId,
        Integer totalQuantity,
        Integer reservedQuantity,
        Integer availableQuantity
) {
}
