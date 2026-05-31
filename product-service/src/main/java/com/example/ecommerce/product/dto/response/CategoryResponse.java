package com.example.ecommerce.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Category details returned in API responses")
public record CategoryResponse(

        @Schema(description = "Unique category ID", example = "1")
        Long id,

        @Schema(description = "Display name of the category", example = "Electronics")
        String name,

        @Schema(description = "Short unique identifier code", example = "ELEC")
        String code,

        @Schema(description = "Optional human-readable description", example = "Consumer electronics and gadgets")
        String description,

        @Schema(description = "Whether the category is visible to customers", example = "true")
        Boolean isActive,

        @Schema(description = "Timestamp when the category was created", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Timestamp of the last update", example = "2024-06-20T14:45:00")
        LocalDateTime modifiedAt,

        @Schema(description = "ID of the user who created this category", example = "1")
        Long createdBy,

        @Schema(description = "ID of the user who last modified this category", example = "1")
        Long modifiedBy

) {
}
