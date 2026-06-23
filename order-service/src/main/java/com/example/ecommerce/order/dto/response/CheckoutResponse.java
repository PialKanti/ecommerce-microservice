package com.example.ecommerce.order.dto.response;

public record CheckoutResponse(
        OrderResponse order,
        PaymentClientResponse payment
) {
}
