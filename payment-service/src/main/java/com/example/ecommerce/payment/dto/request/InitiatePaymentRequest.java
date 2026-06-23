package com.example.ecommerce.payment.dto.request;

import com.example.ecommerce.commons.event.OrderItemPayload;

import java.util.List;
import java.util.UUID;

public record InitiatePaymentRequest(
        Long orderId,
        UUID orderNumber,
        Long userId,
        List<OrderItemPayload> items,
        Double totalAmount
) {
}
