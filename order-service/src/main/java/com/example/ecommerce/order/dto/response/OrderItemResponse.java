package com.example.ecommerce.order.dto.response;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        Double unitPrice,
        Integer quantity,
        Double totalPrice
) {
}
