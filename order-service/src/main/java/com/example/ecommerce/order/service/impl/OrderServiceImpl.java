package com.example.ecommerce.order.service.impl;

import com.example.ecommerce.commons.exception.ResourceConflictException;
import com.example.ecommerce.order.client.CartServiceClient;
import com.example.ecommerce.order.client.InventoryServiceClient;
import com.example.ecommerce.order.client.dto.CartClientResponse;
import com.example.ecommerce.order.client.dto.CartItemClientResponse;
import com.example.ecommerce.order.client.dto.InventoryQuantityRequest;
import com.example.ecommerce.order.dto.response.OrderResponse;
import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.entity.OrderItem;
import com.example.ecommerce.order.entity.OrderStatus;
import com.example.ecommerce.order.mapper.OrderMapper;
import com.example.ecommerce.order.repository.OrderRepository;
import com.example.ecommerce.order.service.OrderCancellationService;
import com.example.ecommerce.order.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartServiceClient cartServiceClient;
    private final InventoryServiceClient inventoryServiceClient;
    private final OrderCancellationService orderCancellationService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse placeOrder(Long userId) {
        log.info("Starting checkout for userId={}", userId);

        // 1. Fetch the user's current cart
        CartClientResponse cart = cartServiceClient.getCart(userId).getData();
        validateCartHasItems(cart, userId);

        // 2. Build order skeleton
        Order order = Order.builder()
                .orderNumber(UUID.randomUUID())
                .userId(userId)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(0.0)
                .createdBy(userId)
                .modifiedBy(userId)
                .build();

        // 3. Reserve inventory and build item snapshots — SAGA Step 1 seam
        cart.items().forEach(cartItem -> {
            reserveInventory(cartItem.productId(), cartItem.quantity());
            order.getItems().add(toOrderItem(order, cartItem));
        });

        // 4. Calculate total and persist — SAGA Step 2 seam
        order.setTotalAmount(calculateTotalAmount(order));
        Order saved = orderRepository.saveAndFlush(order);

        // 5. Clear the cart — SAGA Step 3 seam
        cartServiceClient.clearCart(userId);

        log.info("Checkout completed for userId={}, orderId={}", userId, saved.getId());
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        log.info("Starting order cancellation for userId={}, orderId={}", userId, orderId);

        Order order = findOrder(orderId);
        validateOrderOwnership(order, userId);
        orderCancellationService.cancelConfirmedOrder(order, userId);

        Order saved = orderRepository.saveAndFlush(order);
        log.info("Order cancellation completed for userId={}, orderId={}", userId, orderId);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long userId, Long orderId) {
        Order order = findOrder(orderId);
        validateOrderOwnership(order, userId);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderForAdministration(Long orderId) {
        return orderMapper.toResponse(findOrder(orderId));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrderForAdministration(Long actorUserId, Long orderId) {
        log.info("Starting administrative order cancellation for actorUserId={}, orderId={}", actorUserId, orderId);

        Order order = findOrder(orderId);
        orderCancellationService.cancelConfirmedOrder(order, actorUserId);

        Order saved = orderRepository.saveAndFlush(order);
        log.info("Administrative order cancellation completed for actorUserId={}, orderId={}", actorUserId, orderId);
        return orderMapper.toResponse(saved);
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    }

    private void validateCartHasItems(CartClientResponse cart, Long userId) {
        if (cart.items() == null || cart.items().isEmpty()) {
            log.warn("Checkout rejected because cart is empty for userId={}", userId);
            throw new ResourceConflictException("Cannot checkout an empty cart.");
        }
    }

    private void validateOrderOwnership(Order order, Long userId) {
        if (!order.getUserId().equals(userId)) {
            log.warn("Order ownership validation failed for userId={}, orderId={}", userId, order.getId());
            throw new ResourceConflictException("Order does not belong to current user: " + order.getId());
        }
    }

    private void reserveInventory(Long productId, Integer quantity) {
        inventoryServiceClient.reserve(productId, new InventoryQuantityRequest(quantity));
    }

    private OrderItem toOrderItem(Order order, CartItemClientResponse cartItem) {
        return OrderItem.builder()
                .order(order)
                .productId(cartItem.productId())
                .productName(cartItem.productName())
                .unitPrice(cartItem.unitPrice())
                .quantity(cartItem.quantity())
                .totalPrice(cartItem.unitPrice() * cartItem.quantity())
                .build();
    }

    private Double calculateTotalAmount(Order order) {
        return order.getItems()
                .stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }
}
