package com.quickbite.orderservice.messaging;

import com.quickbite.orderservice.dto.OrderResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent implements Serializable {
    private OrderEventType eventType;
    private Long orderId;
    private Long restaurantId;
    private String restaurantName;
    private String customerEmail;
    private String deliveryAgentEmail;
    private String orderStatus;
    private String deliveryAgentStatus;
    private OrderResponse order;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
