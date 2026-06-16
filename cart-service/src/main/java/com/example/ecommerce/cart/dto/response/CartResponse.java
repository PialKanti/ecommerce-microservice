package com.example.ecommerce.cart.dto.response;

import java.time.LocalDateTime;
import java.util.List;

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
