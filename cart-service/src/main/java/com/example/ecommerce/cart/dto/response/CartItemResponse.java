package com.example.ecommerce.cart.dto.response;

import lombok.Builder;

@Builder
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
