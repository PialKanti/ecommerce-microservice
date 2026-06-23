package com.example.ecommerce.order.client;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.order.client.dto.InitiatePaymentRequest;
import com.example.ecommerce.order.dto.response.PaymentClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentServiceClient {

    @PostMapping(ApiEndpoints.Payment.BASE_PAYMENTS)
    ApiResponse<PaymentClientResponse> createPaymentSession(@RequestBody InitiatePaymentRequest request);
}
