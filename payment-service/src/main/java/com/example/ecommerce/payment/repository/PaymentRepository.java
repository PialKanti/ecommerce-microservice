package com.example.ecommerce.payment.repository;

import com.example.ecommerce.payment.entity.Payment;
import com.example.ecommerce.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findBySessionId(String sessionId);

    Optional<Payment> findTopByOrderIdAndStatusOrderByCreatedAtDesc(Long orderId, PaymentStatus status);

    Optional<Payment> findTopByOrderIdOrderByCreatedAtDesc(Long orderId);

    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.expiresAt <= :now")
    List<Payment> findExpiredPayments(@Param("status") PaymentStatus status,
                                      @Param("now") LocalDateTime now);
}
