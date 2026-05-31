package com.example.ecommerce.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for updating an existing category's name and description")
public record CategoryUpdateRequest(

        @Schema(description = "Updated display name", example = "Consumer Electronics", maxLength = 120)
        @NotBlank(message = "Category name is required")
        @Size(max = 120, message = "Category name cannot exceed 120 characters")
        String name,

        @Schema(description = "Updated description", example = "All consumer electronic devices and accessories", maxLength = 500)
        @Size(max = 500, message = "Category description cannot exceed 500 characters")
        String description

) {
}
