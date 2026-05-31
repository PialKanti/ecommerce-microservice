package com.example.ecommerce.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for updating an existing product. SKU is immutable and cannot be changed.")
public record ProductUpdateRequest(

        @Schema(description = "Updated product display name", example = "Smartphone Pro 256GB", maxLength = 200)
        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name cannot exceed 200 characters")
        String name,

        @Schema(description = "Updated product description", example = "Android smartphone with 256 GB internal storage and 50 MP camera", maxLength = 1000)
        @Size(max = 1000, message = "Product description cannot exceed 1000 characters")
        String description,

        @Schema(description = "Updated retail price in USD; must be greater than zero", example = "599.99", minimum = "0.01")
        @NotNull(message = "Product price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Product price must be greater than zero")
        Double price,

        @Schema(description = "Whether the product is visible to customers", example = "true")
        @NotNull(message = "Product active status is required")
        Boolean isActive,

        @Schema(description = "ID of the parent category", example = "1")
        @NotNull(message = "Product category ID is required")
        Long categoryId,

        @Schema(description = "Updated absolute URL of the product's primary image", example = "https://cdn.example.com/products/phone-001-256.jpg", maxLength = 250)
        @Size(max = 250, message = "Product image URL cannot exceed 250 characters")
        String imageUrl

) {
}
