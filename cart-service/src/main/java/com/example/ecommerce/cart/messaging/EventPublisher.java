package com.example.ecommerce.cart.messaging;

import com.example.ecommerce.cart.config.RabbitMQConfig;
import com.example.ecommerce.commons.event.CartClearFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishCartClearFailed(CartClearFailedEvent event) {
        log.warn("Publishing CartClearFailedEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_CART_CLEAR_FAILED, event);
    }
}
