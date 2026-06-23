package com.example.ecommerce.payment.mapper;

import com.example.ecommerce.payment.dto.response.PaymentResponse;
import com.example.ecommerce.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    default PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .orderNumber(payment.getOrderNumber())
                .sessionId(payment.getSessionId())
                .paymentLink(payment.getPaymentLink())
                .status(payment.getStatus())
                .expiresAt(payment.getExpiresAt())
                .createdAt(payment.getCreatedAt())
                .modifiedAt(payment.getModifiedAt())
                .build();
    }
}
