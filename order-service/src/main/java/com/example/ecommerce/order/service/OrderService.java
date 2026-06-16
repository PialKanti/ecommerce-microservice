package com.example.ecommerce.order.service;

import com.example.ecommerce.order.dto.response.OrderResponse;

public interface OrderService {

    OrderResponse placeOrder(Long userId);

    OrderResponse cancelOrder(Long userId, Long orderId);

    OrderResponse getOrder(Long userId, Long orderId);

    OrderResponse getOrderForAdministration(Long orderId);

    OrderResponse cancelOrderForAdministration(Long actorUserId, Long orderId);
}
