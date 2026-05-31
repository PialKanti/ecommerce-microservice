package com.example.ecommerce.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
        @NotBlank(message = "Category name is required")
        @Size(max = 120, message = "Category name cannot exceed 120 characters")
        String name,

        @NotBlank(message = "Category code is required")
        @Size(max = 50, message = "Category code cannot exceed 50 characters")
        String code,

        @Size(max = 500, message = "Category description cannot exceed 500 characters")
        String description
) {
}
