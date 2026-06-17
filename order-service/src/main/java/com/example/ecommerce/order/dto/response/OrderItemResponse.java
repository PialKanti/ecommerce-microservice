package com.example.ecommerce.order.dto.response;

import lombok.Builder;

@Builder
public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        Double unitPrice,
        Integer quantity,
        Double totalPrice
) {
}
