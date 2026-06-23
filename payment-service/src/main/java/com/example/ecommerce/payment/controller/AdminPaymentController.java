package com.example.ecommerce.payment.controller;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.payment.dto.response.PaymentResponse;
import com.example.ecommerce.payment.mapper.PaymentMapper;
import com.example.ecommerce.payment.repository.PaymentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.Admin.BASE_ADMIN_PAYMENTS)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Payments", description = "Payment lookup endpoints for ADMIN and SUPPORT_AGENT roles.")
public class AdminPaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by internal ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(@PathVariable Long id) {
        PaymentResponse response = paymentRepository.findById(id)
                .map(paymentMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "Get latest payment for an order")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByOrderId(@PathVariable Long orderId) {
        PaymentResponse response = paymentRepository
                .findTopByOrderIdOrderByCreatedAtDesc(orderId)
                .map(paymentMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("No payment found for orderId: " + orderId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
