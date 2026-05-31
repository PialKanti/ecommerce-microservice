package com.example.ecommerce.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Product details returned in API responses")
public record ProductResponse(

        @Schema(description = "Unique product ID", example = "1")
        Long id,

        @Schema(description = "Stock-keeping unit — unique product identifier", example = "PHONE-001")
        String sku,

        @Schema(description = "Product display name", example = "Smartphone Pro 128GB")
        String name,

        @Schema(description = "Detailed product description", example = "Android smartphone with 128 GB internal storage and 50 MP camera")
        String description,

        @Schema(description = "Retail price in USD", example = "499.99")
        Double price,

        @Schema(description = "Whether the product is visible to customers", example = "true")
        Boolean isActive,

        @Schema(description = "ID of the category this product belongs to", example = "1")
        Long categoryId,

        @Schema(description = "Absolute URL of the product's primary image", example = "https://cdn.example.com/products/phone-001.jpg")
        String imageUrl,

        @Schema(description = "Timestamp when the product was created", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Timestamp of the last update", example = "2024-06-20T14:45:00")
        LocalDateTime modifiedAt,

        @Schema(description = "ID of the user who created this product", example = "1")
        Long createdBy,

        @Schema(description = "ID of the user who last modified this product", example = "1")
        Long modifiedBy

) {
}
