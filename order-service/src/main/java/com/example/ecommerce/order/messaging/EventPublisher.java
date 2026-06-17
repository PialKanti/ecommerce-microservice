package com.example.ecommerce.order.messaging;

import com.example.ecommerce.commons.event.OrderCancelledEvent;
import com.example.ecommerce.commons.event.OrderConfirmedEvent;
import com.example.ecommerce.commons.event.OrderCreatedEvent;
import com.example.ecommerce.order.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_ORDER_CREATED, event);
    }

    public void publishOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Publishing OrderConfirmedEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_ORDER_CONFIRMED, event);
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        log.info("Publishing OrderCancelledEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_ORDER_CANCELLED, event);
    }
}
