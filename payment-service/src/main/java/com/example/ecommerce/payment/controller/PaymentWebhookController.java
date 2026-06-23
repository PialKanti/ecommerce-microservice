package com.example.ecommerce.payment.controller;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.Payment.BASE_PAYMENT_WEBHOOK)
@RequiredArgsConstructor
@Tag(name = "Payment Webhooks", description = "Stripe hosted checkout redirect callbacks — no authentication required.")
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @GetMapping("/success")
    @Operation(summary = "Handle successful Stripe payment redirect")
    public ResponseEntity<ApiResponse<String>> handleSuccess(@RequestParam("session_id") String sessionId) {
        paymentService.handleSuccessfulPayment(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Payment successful."));
    }

    @GetMapping("/cancel")
    @Operation(summary = "Handle cancelled Stripe payment redirect")
    public ResponseEntity<ApiResponse<String>> handleCancel(@RequestParam("session_id") String sessionId) {
        paymentService.handleFailedPayment(sessionId, "Payment cancelled by customer.");
        return ResponseEntity.ok(ApiResponse.success("Payment cancelled."));
    }
}
