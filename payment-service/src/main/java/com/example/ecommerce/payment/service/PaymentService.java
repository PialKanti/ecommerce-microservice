package com.example.ecommerce.payment.service;

import com.example.ecommerce.commons.event.OrderItemPayload;
import com.example.ecommerce.payment.dto.response.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse createPaymentSession(Long orderId, UUID orderNumber, Long userId,
                                         List<OrderItemPayload> items, Double totalAmount);

    PaymentResponse initiatePayment(Long orderId, UUID orderNumber, Long userId,
                                    List<OrderItemPayload> items, Double totalAmount);

    void handleSuccessfulPayment(String sessionId);

    void handleFailedPayment(String sessionId, String reason);

    void expireStripeSession(String sessionId);
}
