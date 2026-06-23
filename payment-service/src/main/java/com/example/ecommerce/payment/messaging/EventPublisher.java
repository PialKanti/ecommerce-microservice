package com.example.ecommerce.payment.messaging;

import com.example.ecommerce.commons.event.PaymentFailedEvent;
import com.example.ecommerce.commons.event.PaymentInitiatedEvent;
import com.example.ecommerce.commons.event.PaymentSucceededEvent;
import com.example.ecommerce.payment.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentInitiated(PaymentInitiatedEvent event) {
        log.info("Publishing PaymentInitiatedEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_PAYMENT_INITIATED, event);
    }

    public void publishPaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Publishing PaymentSucceededEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_PAYMENT_SUCCEEDED, event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("Publishing PaymentFailedEvent: orderId={}, reason={}", event.getOrderId(), event.getReason());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_PAYMENT_FAILED, event);
    }
}
