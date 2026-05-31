package com.example.ecommerce.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for creating a new product category")
public record CategoryCreateRequest(

        @Schema(description = "Display name of the category", example = "Electronics", maxLength = 120)
        @NotBlank(message = "Category name is required")
        @Size(max = 120, message = "Category name cannot exceed 120 characters")
        String name,

        @Schema(description = "Short unique identifier code, uppercase recommended", example = "ELEC", maxLength = 50)
        @NotBlank(message = "Category code is required")
        @Size(max = 50, message = "Category code cannot exceed 50 characters")
        String code,

        @Schema(description = "Optional human-readable description", example = "Consumer electronics and gadgets", maxLength = 500)
        @Size(max = 500, message = "Category description cannot exceed 500 characters")
        String description

) {
}
