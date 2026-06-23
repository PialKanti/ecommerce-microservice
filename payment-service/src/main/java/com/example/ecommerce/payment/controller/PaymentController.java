package com.example.ecommerce.payment.controller;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.payment.dto.request.InitiatePaymentRequest;
import com.example.ecommerce.payment.dto.response.PaymentResponse;
import com.example.ecommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.Payment.BASE_PAYMENTS)
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment session management.")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create payment session",
            description = "Creates a Stripe checkout session for the given order. Called internally by order-service during checkout.")
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPaymentSession(
            @RequestBody InitiatePaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment session created.",
                paymentService.createPaymentSession(
                        request.orderId(),
                        request.orderNumber(),
                        request.userId(),
                        request.items(),
                        request.totalAmount())));
    }
}
