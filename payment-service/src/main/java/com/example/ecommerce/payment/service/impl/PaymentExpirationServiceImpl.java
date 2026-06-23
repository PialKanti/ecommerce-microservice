package com.example.ecommerce.payment.service.impl;

import com.example.ecommerce.payment.entity.Payment;
import com.example.ecommerce.payment.entity.PaymentStatus;
import com.example.ecommerce.payment.repository.PaymentRepository;
import com.example.ecommerce.payment.service.PaymentExpirationService;
import com.example.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentExpirationServiceImpl implements PaymentExpirationService {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @Override
    public List<Payment> findExpiredPayments(LocalDateTime now) {
        return paymentRepository.findExpiredPayments(PaymentStatus.INITIATED, now);
    }

    @Override
    public void expirePayment(Long paymentId) {
        Optional<Payment> optional = paymentRepository.findById(paymentId);
        if (optional.isEmpty()) {
            log.warn("Payment not found for expiration: id={}", paymentId);
            return;
        }

        Payment payment = optional.get();

        if (payment.getStatus() != PaymentStatus.INITIATED) {
            log.debug("Payment {} already in status={}, skipping expiration", paymentId, payment.getStatus());
            return;
        }

        if (payment.getExpiresAt().isAfter(LocalDateTime.now())) {
            log.debug("Payment {} expiresAt moved into future, skipping", paymentId);
            return;
        }

        if (hasNewerInitiatedPayment(payment)) {
            paymentService.expireStripeSession(payment.getSessionId());
            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);
            log.info("Silently cancelled superseded payment session: id={}, orderId={}", paymentId, payment.getOrderId());
            return;
        }

        paymentService.expireStripeSession(payment.getSessionId());
        paymentService.handleFailedPayment(payment.getSessionId(), "Payment session expired");
        log.info("Expired payment for orderId={}, paymentId={}", payment.getOrderId(), paymentId);
    }

    private boolean hasNewerInitiatedPayment(Payment payment) {
        return paymentRepository
                .findTopByOrderIdAndStatusOrderByCreatedAtDesc(payment.getOrderId(), PaymentStatus.INITIATED)
                .filter(newer -> !newer.getId().equals(payment.getId()))
                .isPresent();
    }
}
