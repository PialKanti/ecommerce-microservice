package com.example.ecommerce.order.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentClientResponse(
        Long id,
        Long orderId,
        UUID orderNumber,
        String sessionId,
        String paymentLink,
        String status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}
