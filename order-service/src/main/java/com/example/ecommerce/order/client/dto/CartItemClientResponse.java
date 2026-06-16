package com.example.ecommerce.order.client.dto;

public record CartItemClientResponse(
        Long id,
        Long productId,
        String sku,
        String productName,
        Integer quantity,
        Double unitPrice,
        Double lineTotal
) {
}
