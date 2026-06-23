package com.example.ecommerce.cart.config;

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

    public static final String RK_CART_CLEAR_FAILED = "cart.clear.failed";

    // Queue names follow the pattern: <consuming-service>.<routing-key>
    public static final String Q_ORDER_CONFIRMED = "cart.order.confirmed";

    @Bean
    public TopicExchange eventsExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    // ── cart.order.confirmed ──────────────────────────────────────────────────

    @Bean
    public Queue orderConfirmedQueue() {
        return QueueBuilder.durable(Q_ORDER_CONFIRMED)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", Q_ORDER_CONFIRMED)
                .build();
    }

    @Bean
    public Queue orderConfirmedDlq() {
        return QueueBuilder.durable(Q_ORDER_CONFIRMED + ".dlq").build();
    }

    @Bean
    public Binding orderConfirmedBinding(Queue orderConfirmedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(orderConfirmedQueue).to(eventsExchange).with("order.confirmed");
    }

    @Bean
    public Binding orderConfirmedDlqBinding(Queue orderConfirmedDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(orderConfirmedDlq).to(deadLetterExchange).with(Q_ORDER_CONFIRMED);
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
