package com.quickbite.orderservice.messaging;

import com.quickbite.orderservice.config.OrderRabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = OrderRabbitConfig.ORDERS_QUEUE)
    public void onOrderEvent(OrderEvent event) {
        if (event == null) return;
        log.info("Received OrderEvent via RabbitMQ: type={}, orderId={}, status={}",
                event.getEventType(), event.getOrderId(), event.getOrderStatus());

        try {
            // 1. Broadcast to general orders channel (Admin & general listeners)
            messagingTemplate.convertAndSend("/topic/orders", event);

            // 2. Broadcast to specific order channel
            if (event.getOrderId() != null) {
                messagingTemplate.convertAndSend("/topic/orders/" + event.getOrderId(), event);
            }

            // 3. Broadcast to restaurant owner topic
            if (event.getRestaurantId() != null) {
                messagingTemplate.convertAndSend("/topic/restaurants/" + event.getRestaurantId(), event);
            }

            // 4. Broadcast to assigned delivery agent topic
            if (event.getDeliveryAgentEmail() != null && !event.getDeliveryAgentEmail().isBlank()) {
                String agentTopic = "/topic/riders/" + event.getDeliveryAgentEmail().trim().toLowerCase();
                messagingTemplate.convertAndSend(agentTopic, event);
            }

            // 5. Broadcast to customer topic
            if (event.getCustomerEmail() != null && !event.getCustomerEmail().isBlank()) {
                String customerTopic = "/topic/customers/" + event.getCustomerEmail().trim().toLowerCase();
                messagingTemplate.convertAndSend(customerTopic, event);
            }

            log.info("Successfully dispatched STOMP WebSocket notifications for order {}", event.getOrderId());
        } catch (Exception ex) {
            log.error("Failed to broadcast STOMP notification for order {}: {}", event.getOrderId(), ex.getMessage());
        }
    }
}
