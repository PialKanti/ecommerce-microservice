package com.example.ecommerce.cart.messaging;

import com.example.ecommerce.cart.config.RabbitMQConfig;
import com.example.ecommerce.cart.entity.ProcessedEvent;
import com.example.ecommerce.cart.repository.ProcessedEventRepository;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.commons.event.CartClearFailedEvent;
import com.example.ecommerce.commons.event.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartEventListener {

    private final CartService cartService;
    private final ProcessedEventRepository processedEventRepository;
    private final EventPublisher eventPublisher;

    // No @Transactional here — cartService.clearCart() is already @Transactional(REQUIRED).
    // Putting @Transactional on this listener would cause clearCart()'s exception to mark the
    // outer transaction rollback-only, preventing markProcessed() from committing.
    // Each called method manages its own transaction independently.
    @RabbitListener(queues = RabbitMQConfig.Q_ORDER_CONFIRMED)
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent: orderId={}, userId={}", event.getOrderId(), event.getUserId());
        if (isDuplicate(event.getEventId())) return;

        try {
            cartService.clearCart(event.getUserId());
            markProcessed(event.getEventId());
            log.info("Cart cleared for userId={}, orderId={}", event.getUserId(), event.getOrderId());
        } catch (Exception ex) {
            log.error("Cart clear failed for userId={}, orderId={}: {}", event.getUserId(), event.getOrderId(), ex.getMessage());
            eventPublisher.publishCartClearFailed(CartClearFailedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .occurredAt(Instant.now())
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .userId(event.getUserId())
                    .reason(ex.getMessage())
                    .build());
            markProcessed(event.getEventId());
        }
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
