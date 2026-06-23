package com.example.ecommerce.payment.service;

import com.example.ecommerce.payment.entity.Payment;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentExpirationService {

    List<Payment> findExpiredPayments(LocalDateTime now);

    void expirePayment(Long paymentId);
}
