package com.example.ecommerce.inventory.messaging;

import com.example.ecommerce.commons.event.InventoryReservationFailedEvent;
import com.example.ecommerce.commons.event.InventoryReservedEvent;
import com.example.ecommerce.inventory.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishInventoryReserved(InventoryReservedEvent event) {
        log.info("Publishing InventoryReservedEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_INVENTORY_RESERVED, event);
    }

    public void publishInventoryReservationFailed(InventoryReservationFailedEvent event) {
        log.warn("Publishing InventoryReservationFailedEvent: orderId={}, reason={}", event.getOrderId(), event.getReason());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_INVENTORY_RESERVATION_FAILED, event);
    }
}
