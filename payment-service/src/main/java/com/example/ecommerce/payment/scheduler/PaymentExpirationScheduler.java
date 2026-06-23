package com.example.ecommerce.payment.scheduler;

import com.example.ecommerce.payment.entity.Payment;
import com.example.ecommerce.payment.service.PaymentExpirationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentExpirationScheduler {

    private final PaymentExpirationService paymentExpirationService;

    @Scheduled(fixedDelayString = "${payment.expiration.check-delay}")
    public void expireOverduePayments() {
        List<Payment> expired = paymentExpirationService.findExpiredPayments(LocalDateTime.now());
        if (expired.isEmpty()) return;

        log.info("Found {} expired payment(s) to process", expired.size());
        for (Payment payment : expired) {
            try {
                paymentExpirationService.expirePayment(payment.getId());
            } catch (Exception e) {
                log.error("Failed to expire payment id={}: {}", payment.getId(), e.getMessage());
            }
        }
    }
}
