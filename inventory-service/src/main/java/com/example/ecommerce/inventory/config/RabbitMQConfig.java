package com.example.ecommerce.inventory.config;

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

    public static final String RK_INVENTORY_RESERVED           = "inventory.reserved";
    public static final String RK_INVENTORY_RESERVATION_FAILED = "inventory.reservation.failed";

    // Queue names follow the pattern: <consuming-service>.<routing-key>
    public static final String Q_ORDER_CREATED   = "inventory.order.created";
    public static final String Q_ORDER_CANCELLED = "inventory.order.cancelled";
    public static final String Q_ORDER_PAID      = "inventory.order.paid";

    @Bean
    public TopicExchange eventsExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    // ── inventory.order.created ───────────────────────────────────────────────

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(Q_ORDER_CREATED)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", Q_ORDER_CREATED)
                .build();
    }

    @Bean
    public Queue orderCreatedDlq() {
        return QueueBuilder.durable(Q_ORDER_CREATED + ".dlq").build();
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(eventsExchange).with("order.created");
    }

    @Bean
    public Binding orderCreatedDlqBinding(Queue orderCreatedDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(orderCreatedDlq).to(deadLetterExchange).with(Q_ORDER_CREATED);
    }

    // ── inventory.order.cancelled ─────────────────────────────────────────────

    @Bean
    public Queue orderCancelledQueue() {
        return QueueBuilder.durable(Q_ORDER_CANCELLED)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", Q_ORDER_CANCELLED)
                .build();
    }

    @Bean
    public Queue orderCancelledDlq() {
        return QueueBuilder.durable(Q_ORDER_CANCELLED + ".dlq").build();
    }

    @Bean
    public Binding orderCancelledBinding(Queue orderCancelledQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(orderCancelledQueue).to(eventsExchange).with("order.cancelled");
    }

    @Bean
    public Binding orderCancelledDlqBinding(Queue orderCancelledDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(orderCancelledDlq).to(deadLetterExchange).with(Q_ORDER_CANCELLED);
    }

    // ── inventory.order.paid ──────────────────────────────────────────────────

    @Bean
    public Queue orderPaidQueue() {
        return QueueBuilder.durable(Q_ORDER_PAID)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", Q_ORDER_PAID)
                .build();
    }

    @Bean
    public Queue orderPaidDlq() {
        return QueueBuilder.durable(Q_ORDER_PAID + ".dlq").build();
    }

    @Bean
    public Binding orderPaidBinding(Queue orderPaidQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(orderPaidQueue).to(eventsExchange).with("order.paid");
    }

    @Bean
    public Binding orderPaidDlqBinding(Queue orderPaidDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(orderPaidDlq).to(deadLetterExchange).with(Q_ORDER_PAID);
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
