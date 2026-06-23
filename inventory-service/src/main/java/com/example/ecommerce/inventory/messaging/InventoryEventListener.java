package com.example.ecommerce.inventory.messaging;

import com.example.ecommerce.commons.event.InventoryReservationFailedEvent;
import com.example.ecommerce.commons.event.OrderPaidEvent;
import com.example.ecommerce.commons.event.InventoryReservedEvent;
import com.example.ecommerce.commons.event.OrderCancelledEvent;
import com.example.ecommerce.commons.event.OrderCreatedEvent;
import com.example.ecommerce.commons.event.OrderItemPayload;
import com.example.ecommerce.inventory.config.RabbitMQConfig;
import com.example.ecommerce.inventory.entity.Inventory;
import com.example.ecommerce.inventory.entity.ProcessedEvent;
import com.example.ecommerce.inventory.repository.InventoryRepository;
import com.example.ecommerce.inventory.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final InventoryRepository inventoryRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final EventPublisher eventPublisher;

    @RabbitListener(queues = RabbitMQConfig.Q_ORDER_CREATED)
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());
        if (isDuplicate(event.getEventId())) return;

        try {
            for (OrderItemPayload item : event.getItems()) {
                reserveItem(item.getProductId(), item.getQuantity(), event.getOrderId());
            }

            eventPublisher.publishInventoryReserved(InventoryReservedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .occurredAt(Instant.now())
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .userId(event.getUserId())
                    .build());

            markProcessed(event.getEventId());
            log.info("Inventory reserved for orderId={}", event.getOrderId());

        } catch (IllegalStateException ex) {
            log.error("Inventory reservation failed for orderId={}: {}", event.getOrderId(), ex.getMessage());

            eventPublisher.publishInventoryReservationFailed(InventoryReservationFailedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .occurredAt(Instant.now())
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .userId(event.getUserId())
                    .reason(ex.getMessage())
                    .build());

            markProcessed(event.getEventId());
            // Do not re-throw — failure event has been published.
            // ObjectOptimisticLockingFailureException propagates out for Spring Retry → DLQ.
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_ORDER_CANCELLED)
    @Transactional
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Received OrderCancelledEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());
        if (isDuplicate(event.getEventId())) return;

        for (OrderItemPayload item : event.getItems()) {
            releaseItem(item.getProductId(), item.getQuantity(), event.getOrderId());
        }

        markProcessed(event.getEventId());
        log.info("Inventory released for orderId={}", event.getOrderId());
    }

    @RabbitListener(queues = RabbitMQConfig.Q_ORDER_PAID)
    @Transactional
    public void onOrderPaid(OrderPaidEvent event) {
        log.info("Received OrderPaidEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());
        if (isDuplicate(event.getEventId())) return;

        for (OrderItemPayload item : event.getItems()) {
            sellItem(item.getProductId(), item.getQuantity(), event.getOrderId());
        }

        markProcessed(event.getEventId());
        log.info("Inventory decremented (sold) for orderId={}", event.getOrderId());
    }

    private void reserveItem(Long productId, Integer quantity, Long orderId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalStateException(
                        "Inventory not found for product: " + productId));

        int available = inventory.getTotalQuantity() - inventory.getReservedQuantity();
        if (available < quantity) {
            throw new IllegalStateException(
                    "Insufficient stock for product " + productId +
                    ": available=" + available + ", requested=" + quantity);
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventoryRepository.saveAndFlush(inventory);
    }

    private void releaseItem(Long productId, Integer quantity, Long orderId) {
        inventoryRepository.findByProductId(productId).ifPresentOrElse(inventory -> {
            int releasable = Math.min(quantity, inventory.getReservedQuantity());
            if (releasable < quantity) {
                log.warn("Releasing less than requested for product {}: requested={}, releasable={}",
                        productId, quantity, releasable);
            }
            inventory.setReservedQuantity(inventory.getReservedQuantity() - releasable);
            inventoryRepository.saveAndFlush(inventory);
        }, () -> log.warn("Inventory not found for product {} during release for orderId={}", productId, orderId));
    }

    private void sellItem(Long productId, Integer quantity, Long orderId) {
        inventoryRepository.findByProductId(productId).ifPresentOrElse(inventory -> {
            int sellable = Math.min(quantity, inventory.getReservedQuantity());
            if (sellable < quantity) {
                log.warn("Selling less reserved than expected for product {}: expected={}, reservedQuantity={}",
                        productId, quantity, inventory.getReservedQuantity());
            }
            inventory.setReservedQuantity(inventory.getReservedQuantity() - sellable);
            inventory.setTotalQuantity(inventory.getTotalQuantity() - sellable);
            inventoryRepository.saveAndFlush(inventory);
        }, () -> log.warn("Inventory not found for product {} during sell for orderId={}", productId, orderId));
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
