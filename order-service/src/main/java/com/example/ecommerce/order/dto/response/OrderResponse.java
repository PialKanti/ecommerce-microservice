package com.example.ecommerce.order.dto.response;

import com.example.ecommerce.order.entity.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderResponse(
        Long id,
        UUID orderNumber,
        Long userId,
        OrderStatus status,
        Double totalAmount,
        LocalDateTime cancelledAt,
        String paymentLink,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        Long createdBy,
        Long modifiedBy
) {
}
