package com.example.ecommerce.cart.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CartResponse(
        Long id,
        Long userId,
        List<CartItemResponse> items,
        Integer totalQuantity,
        Double subtotal,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        Long createdBy,
        Long modifiedBy
) {
}
