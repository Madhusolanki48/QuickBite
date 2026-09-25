package com.quickbite.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderRabbitConfig {

    public static final String ORDERS_EXCHANGE = "quickbite.orders.exchange";
    public static final String ORDERS_QUEUE = "quickbite.orders.queue";
    public static final String ORDERS_ROUTING_KEY_PREFIX = "orders.";

    public static final String ROUTING_KEY_ORDER_CREATED = "orders.created";
    public static final String ROUTING_KEY_ORDER_STATUS_CHANGED = "orders.status-changed";
    public static final String ROUTING_KEY_ORDER_ASSIGNED = "orders.assigned";
    public static final String ROUTING_KEY_ORDER_DELIVERED = "orders.delivered";

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(ORDERS_EXCHANGE, true, false);
    }

    @Bean
    public Queue ordersQueue() {
        return QueueBuilder.durable(ORDERS_QUEUE).build();
    }

    @Bean
    public Binding ordersBinding(Queue ordersQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(ordersQueue).to(ordersExchange).with("orders.#");
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter rabbitMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(rabbitMessageConverter);
        return template;
    }
}
