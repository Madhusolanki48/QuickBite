package com.quickbite.orderservice.dto;

import com.quickbite.orderservice.model.*;
import java.time.Instant;
import java.util.List;

public record OrderResponse(Long id, Long customerId, Long restaurantId, String customerEmail, double totalAmount,
        OrderStatus orderStatus, PaymentStatus paymentStatus, Instant createdAt, List<OrderItemResponse> items) {
}
