package com.example.ecommerce.payment.messaging;

import com.example.ecommerce.commons.event.OrderConfirmedEvent;
import com.example.ecommerce.payment.config.RabbitMQConfig;
import com.example.ecommerce.payment.entity.ProcessedEvent;
import com.example.ecommerce.payment.repository.ProcessedEventRepository;
import com.example.ecommerce.payment.service.PaymentService;
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
public class PaymentEventListener {

    private final PaymentService paymentService;
    private final ProcessedEventRepository processedEventRepository;

    @RabbitListener(queues = RabbitMQConfig.Q_ORDER_CONFIRMED)
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());
        if (isDuplicate(event.getEventId())) return;

        paymentService.initiatePayment(
                event.getOrderId(),
                event.getOrderNumber(),
                event.getUserId(),
                event.getItems(),
                event.getTotalAmount());

        markProcessed(event.getEventId());
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
