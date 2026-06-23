package com.example.ecommerce.payment.dto.response;

import com.example.ecommerce.payment.entity.PaymentStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PaymentResponse(
        Long id,
        Long orderId,
        UUID orderNumber,
        String sessionId,
        String paymentLink,
        PaymentStatus status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}
