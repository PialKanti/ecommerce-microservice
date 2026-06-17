package com.example.ecommerce.order.service;

import com.example.ecommerce.commons.event.OrderCancelledEvent;
import com.example.ecommerce.commons.event.OrderItemPayload;
import com.example.ecommerce.commons.exception.ResourceConflictException;
import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.entity.OrderStatus;
import com.example.ecommerce.order.messaging.EventPublisher;
import com.example.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCancellationService {

    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public void cancelAndPublish(Order order, String reason, Long modifiedBy) {
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            log.warn("Order cancellation rejected for orderId={} because status={}", order.getId(), order.getStatus());
            throw new ResourceConflictException("Only PENDING or CONFIRMED orders can be cancelled.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setModifiedBy(modifiedBy);
        orderRepository.saveAndFlush(order);

        List<OrderItemPayload> payloads = order.getItems().stream()
                .map(item -> OrderItemPayload.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .toList();

        eventPublisher.publishOrderCancelled(OrderCancelledEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredAt(Instant.now())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .items(payloads)
                .reason(reason)
                .build());

        log.info("Order CANCELLED and OrderCancelledEvent published: orderId={}, reason={}", order.getId(), reason);
    }
}
