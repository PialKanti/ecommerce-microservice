package com.example.ecommerce.payment.service.impl;

import com.example.ecommerce.commons.event.OrderItemPayload;
import com.example.ecommerce.commons.event.PaymentFailedEvent;
import com.example.ecommerce.commons.event.PaymentInitiatedEvent;
import com.example.ecommerce.commons.event.PaymentSucceededEvent;
import com.example.ecommerce.commons.exception.ResourceConflictException;
import com.example.ecommerce.payment.config.PaymentExpirationProperties;
import com.example.ecommerce.payment.config.StripeConfig;
import com.example.ecommerce.payment.dto.response.PaymentResponse;
import com.example.ecommerce.payment.entity.Payment;
import com.example.ecommerce.payment.entity.PaymentStatus;
import com.example.ecommerce.payment.mapper.PaymentMapper;
import com.example.ecommerce.payment.messaging.EventPublisher;
import com.example.ecommerce.payment.repository.PaymentRepository;
import com.example.ecommerce.payment.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionExpireParams;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final StripeConfig stripeConfig;
    private final PaymentExpirationProperties expirationProperties;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public PaymentResponse createPaymentSession(Long orderId, UUID orderNumber, Long userId,
                                                List<OrderItemPayload> items, Double totalAmount) {
        Optional<Payment> existing = paymentRepository
                .findTopByOrderIdAndStatusOrderByCreatedAtDesc(orderId, PaymentStatus.INITIATED);

        if (existing.isPresent()) {
            Payment existingPayment = existing.get();
            if (existingPayment.getExpiresAt().isAfter(LocalDateTime.now())) {
                log.info("Returning existing active payment session for orderId={}", orderId);
                return paymentMapper.toResponse(existingPayment);
            }
            expireStripeSession(existingPayment.getSessionId());
            existingPayment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(existingPayment);
            log.info("Cancelled expired payment session for orderId={}", orderId);
        }

        try {
            Session session = Session.create(buildSessionCreateParams(orderId, orderNumber, userId, items, totalAmount));

            LocalDateTime expiresAt = LocalDateTime.now().plus(expirationProperties.getLifetime());
            Payment payment = Payment.builder()
                    .orderId(orderId)
                    .orderNumber(orderNumber)
                    .userId(userId)
                    .sessionId(session.getId())
                    .paymentLink(session.getUrl())
                    .status(PaymentStatus.INITIATED)
                    .expiresAt(expiresAt)
                    .build();
            paymentRepository.save(payment);

            log.info("Payment session created for orderId={}, sessionId={}", orderId, session.getId());
            return paymentMapper.toResponse(payment);

        } catch (StripeException e) {
            log.error("Stripe session creation failed for orderId={}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to create Stripe payment session: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public PaymentResponse initiatePayment(Long orderId, UUID orderNumber, Long userId,
                                           List<OrderItemPayload> items, Double totalAmount) {
        PaymentResponse response = createPaymentSession(orderId, orderNumber, userId, items, totalAmount);

        eventPublisher.publishPaymentInitiated(PaymentInitiatedEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredAt(Instant.now())
                .orderId(orderId)
                .orderNumber(orderNumber)
                .userId(userId)
                .sessionId(response.sessionId())
                .paymentLink(response.paymentLink())
                .build());

        return response;
    }

    @Override
    @Transactional
    public void handleSuccessfulPayment(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for sessionId: " + sessionId));

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            log.warn("Payment already SUCCEEDED for sessionId={}, skipping", sessionId);
            return;
        }

        if (payment.getStatus() == PaymentStatus.FAILED || payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new ResourceConflictException(
                    "Cannot mark payment as succeeded — current status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.SUCCEEDED);
        paymentRepository.save(payment);

        eventPublisher.publishPaymentSucceeded(PaymentSucceededEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredAt(Instant.now())
                .orderId(payment.getOrderId())
                .orderNumber(payment.getOrderNumber())
                .userId(payment.getUserId())
                .sessionId(sessionId)
                .build());

        log.info("Payment SUCCEEDED for orderId={}, sessionId={}", payment.getOrderId(), sessionId);
    }

    @Override
    @Transactional
    public void handleFailedPayment(String sessionId, String reason) {
        Optional<Payment> optional = paymentRepository.findBySessionId(sessionId);
        if (optional.isEmpty()) {
            log.warn("Payment not found for sessionId={}, nothing to fail", sessionId);
            return;
        }

        Payment payment = optional.get();
        if (payment.getStatus() == PaymentStatus.SUCCEEDED
                || payment.getStatus() == PaymentStatus.FAILED
                || payment.getStatus() == PaymentStatus.CANCELLED) {
            log.warn("Payment already in terminal status={} for sessionId={}, skipping", payment.getStatus(), sessionId);
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        eventPublisher.publishPaymentFailed(PaymentFailedEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredAt(Instant.now())
                .orderId(payment.getOrderId())
                .orderNumber(payment.getOrderNumber())
                .userId(payment.getUserId())
                .sessionId(sessionId)
                .reason(reason)
                .build());

        log.info("Payment FAILED for orderId={}, sessionId={}, reason={}", payment.getOrderId(), sessionId, reason);
    }

    @Override
    public void expireStripeSession(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            session.expire(SessionExpireParams.builder().build());
            log.info("Stripe session expired: {}", sessionId);
        } catch (StripeException e) {
            log.warn("Could not expire Stripe session {}: {}", sessionId, e.getMessage());
        }
    }

    private SessionCreateParams buildSessionCreateParams(Long orderId, UUID orderNumber, Long userId,
                                                          List<OrderItemPayload> items, Double totalAmount) {
        List<SessionCreateParams.LineItem> lineItems = items.stream()
                .map(this::toStripeLineItem)
                .toList();

        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeConfig.getSuccessUrl())
                .setCancelUrl(stripeConfig.getCancelUrl())
                .setClientReferenceId(orderId.toString())
                .putMetadata("orderId", orderId.toString())
                .putMetadata("orderNumber", orderNumber.toString())
                .putMetadata("userId", userId.toString())
                .addAllLineItem(lineItems)
                .build();
    }

    private SessionCreateParams.LineItem toStripeLineItem(OrderItemPayload item) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity((long) item.getQuantity())
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(stripeConfig.getCurrency())
                        .setUnitAmount(toMinorUnitAmount(item.getUnitPrice()))
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(item.getProductName())
                                .build())
                        .build())
                .build();
    }

    private long toMinorUnitAmount(Double amount) {
        return Math.round(amount * 100);
    }
}
