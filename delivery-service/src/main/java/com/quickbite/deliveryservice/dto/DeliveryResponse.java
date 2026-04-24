package com.quickbite.deliveryservice.dto;

import com.quickbite.deliveryservice.model.DeliveryStatus;
import java.time.Instant;

public record DeliveryResponse(Long id, Long orderId, Long riderId, String riderName, String riderPhone,
        String deliveryAddress, DeliveryStatus deliveryStatus, Instant createdAt) {
}
