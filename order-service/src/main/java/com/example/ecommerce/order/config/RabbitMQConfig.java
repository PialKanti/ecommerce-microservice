package com.example.ecommerce.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "ecommerce.topic";
    public static final String DLX      = "ecommerce.dlx";

    public static final String RK_ORDER_CREATED   = "order.created";
    public static final String RK_ORDER_CONFIRMED = "order.confirmed";
    public static final String RK_ORDER_CANCELLED = "order.cancelled";

    // Queue names follow the pattern: <consuming-service>.<routing-key>
    public static final String Q_INV_RESERVED      = "order.inventory.reserved";
    public static final String Q_INV_RES_FAILED    = "order.inventory.reservation.failed";
    public static final String Q_CART_CLEAR_FAILED = "order.cart.clear.failed";

    @Bean
    public TopicExchange eventsExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    // ── order.inventory.reserved ──────────────────────────────────────────────

    @Bean
    public Queue invReservedQueue() {
        return QueueBuilder.durable(Q_INV_RESERVED)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", Q_INV_RESERVED)
                .build();
    }

    @Bean
    public Queue invReservedDlq() {
        return QueueBuilder.durable(Q_INV_RESERVED + ".dlq").build();
    }

    @Bean
    public Binding invReservedBinding(Queue invReservedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(invReservedQueue).to(eventsExchange).with("inventory.reserved");
    }

    @Bean
    public Binding invReservedDlqBinding(Queue invReservedDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(invReservedDlq).to(deadLetterExchange).with(Q_INV_RESERVED);
    }

    // ── order.inventory.reservation.failed ────────────────────────────────────

    @Bean
    public Queue invResFailedQueue() {
        return QueueBuilder.durable(Q_INV_RES_FAILED)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", Q_INV_RES_FAILED)
                .build();
    }

    @Bean
    public Queue invResFailedDlq() {
        return QueueBuilder.durable(Q_INV_RES_FAILED + ".dlq").build();
    }

    @Bean
    public Binding invResFailedBinding(Queue invResFailedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(invResFailedQueue).to(eventsExchange).with("inventory.reservation.failed");
    }

    @Bean
    public Binding invResFailedDlqBinding(Queue invResFailedDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(invResFailedDlq).to(deadLetterExchange).with(Q_INV_RES_FAILED);
    }

    // ── order.cart.clear.failed ───────────────────────────────────────────────

    @Bean
    public Queue cartClearFailedQueue() {
        return QueueBuilder.durable(Q_CART_CLEAR_FAILED)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", Q_CART_CLEAR_FAILED)
                .build();
    }

    @Bean
    public Queue cartClearFailedDlq() {
        return QueueBuilder.durable(Q_CART_CLEAR_FAILED + ".dlq").build();
    }

    @Bean
    public Binding cartClearFailedBinding(Queue cartClearFailedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(cartClearFailedQueue).to(eventsExchange).with("cart.clear.failed");
    }

    @Bean
    public Binding cartClearFailedDlqBinding(Queue cartClearFailedDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(cartClearFailedDlq).to(deadLetterExchange).with(Q_CART_CLEAR_FAILED);
    }

    // ── Messaging infrastructure ──────────────────────────────────────────────

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter);
        return template;
    }

    @Bean
    public DirectRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        DirectRabbitListenerContainerFactory factory = new DirectRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter);
        factory.setTaskExecutor(Executors.newVirtualThreadPerTaskExecutor());
        return factory;
    }
}
