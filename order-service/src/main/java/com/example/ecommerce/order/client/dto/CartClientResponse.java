package com.example.ecommerce.order.client.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CartClientResponse(
        Long id,
        Long userId,
        List<CartItemClientResponse> items,
        Integer totalQuantity,
        Double subtotal,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        Long createdBy,
        Long modifiedBy
) {
}
