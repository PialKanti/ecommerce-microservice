package com.example.ecommerce.order.messaging;

import com.example.ecommerce.commons.event.CartClearFailedEvent;
import com.example.ecommerce.commons.event.InventoryReservationFailedEvent;
import com.example.ecommerce.commons.event.InventoryReservedEvent;
import com.example.ecommerce.commons.event.OrderConfirmedEvent;
import com.example.ecommerce.commons.event.OrderItemPayload;
import com.example.ecommerce.commons.event.PaymentFailedEvent;
import com.example.ecommerce.commons.event.PaymentInitiatedEvent;
import com.example.ecommerce.commons.event.PaymentSucceededEvent;
import com.example.ecommerce.order.config.RabbitMQConfig;
import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.entity.OrderStatus;
import com.example.ecommerce.order.entity.ProcessedEvent;
import com.example.ecommerce.order.repository.OrderRepository;
import com.example.ecommerce.order.repository.ProcessedEventRepository;
import com.example.ecommerce.order.service.OrderCancellationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final EventPublisher eventPublisher;
    private final OrderCancellationService cancellationService;

    @RabbitListener(queues = RabbitMQConfig.Q_INV_RESERVED)
    @Transactional
    public void onInventoryReserved(InventoryReservedEvent event) {
        log.info("Received InventoryReservedEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());
        if (isDuplicate(event.getEventId())) return;

        Order order = findOrder(event.getOrderId());

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order {} is not PENDING (status={}), skipping confirmation", order.getId(), order.getStatus());
            markProcessed(event.getEventId());
            return;
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.saveAndFlush(order);

        List<OrderItemPayload> payloads = order.getItems().stream()
                .map(item -> OrderItemPayload.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .toList();

        eventPublisher.publishOrderConfirmed(OrderConfirmedEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredAt(Instant.now())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .items(payloads)
                .build());

        markProcessed(event.getEventId());
        log.info("Order CONFIRMED: orderId={}", order.getId());
    }

    @RabbitListener(queues = RabbitMQConfig.Q_INV_RES_FAILED)
    @Transactional
    public void onInventoryReservationFailed(InventoryReservationFailedEvent event) {
        log.warn("Received InventoryReservationFailedEvent: orderId={}, reason={}", event.getOrderId(), event.getReason());
        if (isDuplicate(event.getEventId())) return;

        Order order = findOrder(event.getOrderId());

        if (order.getStatus() == OrderStatus.PENDING) {
            cancellationService.cancelAndPublish(order, event.getReason(), null);
        } else {
            log.warn("Order {} already in status={}, skipping SAGA cancellation", order.getId(), order.getStatus());
        }

        markProcessed(event.getEventId());
    }

    @RabbitListener(queues = RabbitMQConfig.Q_CART_CLEAR_FAILED)
    @Transactional
    public void onCartClearFailed(CartClearFailedEvent event) {
        log.warn("Received CartClearFailedEvent: orderId={}, reason={}", event.getOrderId(), event.getReason());
        if (isDuplicate(event.getEventId())) return;

        Order order = findOrder(event.getOrderId());

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            cancellationService.cancelAndPublish(order, "Cart clear failed: " + event.getReason(), null);
        } else {
            log.warn("Order {} not CONFIRMED (status={}), skipping cart-clear-failed compensation",
                    order.getId(), order.getStatus());
        }

        markProcessed(event.getEventId());
    }

    @RabbitListener(queues = RabbitMQConfig.Q_PAYMENT_INITIATED)
    @Transactional
    public void onPaymentInitiated(PaymentInitiatedEvent event) {
        log.info("Received PaymentInitiatedEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());
        if (isDuplicate(event.getEventId())) return;

        Order order = findOrder(event.getOrderId());

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            log.warn("Order {} is not CONFIRMED (status={}), skipping AWAITING_PAYMENT transition",
                    order.getId(), order.getStatus());
            markProcessed(event.getEventId());
            return;
        }

        order.setStatus(OrderStatus.AWAITING_PAYMENT);
        order.setPaymentLink(event.getPaymentLink());
        orderRepository.saveAndFlush(order);
        markProcessed(event.getEventId());
        log.info("Order AWAITING_PAYMENT: orderId={}", order.getId());
    }

    @RabbitListener(queues = RabbitMQConfig.Q_PAYMENT_SUCCEEDED)
    @Transactional
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Received PaymentSucceededEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());
        if (isDuplicate(event.getEventId())) return;

        Order order = findOrder(event.getOrderId());

        if (order.getStatus() == OrderStatus.PAID) {
            log.warn("Order {} already PAID, skipping", order.getId());
            markProcessed(event.getEventId());
            return;
        }

        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            log.warn("Order {} is not AWAITING_PAYMENT (status={}), skipping PAID transition",
                    order.getId(), order.getStatus());
            markProcessed(event.getEventId());
            return;
        }

        order.setStatus(OrderStatus.PAID);
        orderRepository.saveAndFlush(order);
        markProcessed(event.getEventId());
        log.info("Order PAID: orderId={}", order.getId());
    }

    @RabbitListener(queues = RabbitMQConfig.Q_PAYMENT_FAILED)
    @Transactional
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.warn("Received PaymentFailedEvent: orderId={}, reason={}", event.getOrderId(), event.getReason());
        if (isDuplicate(event.getEventId())) return;

        Order order = findOrder(event.getOrderId());

        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("Order {} already CANCELLED, skipping", order.getId());
            markProcessed(event.getEventId());
            return;
        }

        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            log.warn("Order {} is not AWAITING_PAYMENT (status={}), skipping payment-failed compensation",
                    order.getId(), order.getStatus());
            markProcessed(event.getEventId());
            return;
        }

        cancellationService.cancelAndPublish(order, event.getReason(), null);
        markProcessed(event.getEventId());
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found in SAGA: " + orderId));
    }

    private boolean isDuplicate(UUID eventId) {
        if (processedEventRepository.existsById(eventId)) {
            log.warn("Duplicate event suppressed: eventId={}", eventId);
            return true;
        }
        return false;
    }

    private void markProcessed(UUID eventId) {
        try {
            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(eventId)
                    .processedAt(Instant.now())
                    .build());
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent duplicate event suppressed at insert: eventId={}", eventId);
        }
    }
}
