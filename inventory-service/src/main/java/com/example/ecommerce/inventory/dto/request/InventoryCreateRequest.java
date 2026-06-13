package com.example.ecommerce.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryCreateRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Total quantity is required")
        @Min(value = 0, message = "Total quantity must be 0 or greater")
        Integer totalQuantity
) {
}
