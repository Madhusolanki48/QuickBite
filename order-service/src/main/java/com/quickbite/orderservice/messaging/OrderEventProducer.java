package com.quickbite.orderservice.messaging;

import com.quickbite.orderservice.config.OrderRabbitConfig;
import com.quickbite.orderservice.dto.OrderResponse;
import com.quickbite.orderservice.model.FoodOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(FoodOrder order, OrderResponse response) {
        OrderEvent event = OrderEvent.builder()
                .eventType(OrderEventType.ORDER_CREATED)
                .orderId(order.getId())
                .restaurantId(order.getRestaurantId())
                .customerEmail(order.getCustomerEmail())
                .deliveryAgentEmail(order.getDeliveryAgentEmail())
                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : "CREATED")
                .deliveryAgentStatus(order.getDeliveryAgentStatus() != null ? order.getDeliveryAgentStatus().name() : "UNASSIGNED")
                .order(response)
                .timestamp(Instant.now())
                .build();
        publish(OrderRabbitConfig.ROUTING_KEY_ORDER_CREATED, event);
    }

    public void publishOrderStatusChanged(FoodOrder order, OrderResponse response) {
        OrderEventType type = (order.getOrderStatus() != null && order.getOrderStatus().name().equals("DELIVERED"))
                ? OrderEventType.ORDER_DELIVERED
                : OrderEventType.ORDER_STATUS_CHANGED;
        String routingKey = (type == OrderEventType.ORDER_DELIVERED)
                ? OrderRabbitConfig.ROUTING_KEY_ORDER_DELIVERED
                : OrderRabbitConfig.ROUTING_KEY_ORDER_STATUS_CHANGED;

        OrderEvent event = OrderEvent.builder()
                .eventType(type)
                .orderId(order.getId())
                .restaurantId(order.getRestaurantId())
                .customerEmail(order.getCustomerEmail())
                .deliveryAgentEmail(order.getDeliveryAgentEmail())
                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : "")
                .deliveryAgentStatus(order.getDeliveryAgentStatus() != null ? order.getDeliveryAgentStatus().name() : "")
                .order(response)
                .timestamp(Instant.now())
                .build();
        publish(routingKey, event);
    }

    public void publishOrderAssigned(FoodOrder order, OrderResponse response) {
        OrderEvent event = OrderEvent.builder()
                .eventType(OrderEventType.ORDER_ASSIGNED)
                .orderId(order.getId())
                .restaurantId(order.getRestaurantId())
                .customerEmail(order.getCustomerEmail())
                .deliveryAgentEmail(order.getDeliveryAgentEmail())
                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : "")
                .deliveryAgentStatus(order.getDeliveryAgentStatus() != null ? order.getDeliveryAgentStatus().name() : "ASSIGNED")
                .order(response)
                .timestamp(Instant.now())
                .build();
        publish(OrderRabbitConfig.ROUTING_KEY_ORDER_ASSIGNED, event);
    }

    private void publish(String routingKey, OrderEvent event) {
        try {
            log.info("Publishing {} event to exchange {} with routingKey {}",
                    event.getEventType(), OrderRabbitConfig.ORDERS_EXCHANGE, routingKey);
            rabbitTemplate.convertAndSend(OrderRabbitConfig.ORDERS_EXCHANGE, routingKey, event);
        } catch (Exception ex) {
            log.warn("Failed to publish RabbitMQ event for order {}: {}", event.getOrderId(), ex.getMessage());
        }
    }
}
