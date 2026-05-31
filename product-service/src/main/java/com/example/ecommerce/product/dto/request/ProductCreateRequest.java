package com.example.ecommerce.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for creating a new product")
public record ProductCreateRequest(

        @Schema(description = "Stock-keeping unit — unique, immutable product identifier", example = "PHONE-001", maxLength = 100)
        @NotBlank(message = "Product SKU is required")
        @Size(max = 100, message = "Product SKU cannot exceed 100 characters")
        String sku,

        @Schema(description = "Product display name shown to customers", example = "Smartphone Pro 128GB", maxLength = 200)
        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name cannot exceed 200 characters")
        String name,

        @Schema(description = "Detailed product description", example = "Android smartphone with 128 GB internal storage and 50 MP camera", maxLength = 1000)
        @Size(max = 1000, message = "Product description cannot exceed 1000 characters")
        String description,

        @Schema(description = "Retail price in USD; must be greater than zero", example = "499.99", minimum = "0.01")
        @NotNull(message = "Product price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Product price must be greater than zero")
        Double price,

        @Schema(description = "Whether the product is visible to customers. Defaults to `true` when omitted", example = "true")
        Boolean isActive,

        @Schema(description = "ID of the parent category this product belongs to", example = "1")
        @NotNull(message = "Product category ID is required")
        Long categoryId,

        @Schema(description = "Absolute URL of the product's primary image", example = "https://cdn.example.com/products/phone-001.jpg", maxLength = 250)
        @Size(max = 250, message = "Product image URL cannot exceed 250 characters")
        String imageUrl

) {
}
