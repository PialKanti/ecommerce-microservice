package com.example.ecommerce.cart.dto.response;

public record CartItemResponse(
        Long id,
        Long productId,
        String sku,
        String productName,
        Integer quantity,
        Double unitPrice,
        Double lineTotal
) {
}
