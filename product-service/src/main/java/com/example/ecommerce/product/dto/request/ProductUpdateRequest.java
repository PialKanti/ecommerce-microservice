package com.example.ecommerce.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name cannot exceed 200 characters")
        String name,

        @Size(max = 1000, message = "Product description cannot exceed 1000 characters")
        String description,

        @NotNull(message = "Product price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Product price must be greater than zero")
        Double price,

        @NotNull(message = "Product active status is required")
        Boolean isActive,

        @NotNull(message = "Product category ID is required")
        Long categoryId,

        @Size(max = 250, message = "Product image URL cannot exceed 250 characters")
        String imageUrl
) {
}
