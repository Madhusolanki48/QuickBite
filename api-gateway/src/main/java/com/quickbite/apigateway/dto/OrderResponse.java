package com.quickbite.apigateway.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        Long restaurantId,
        String customerEmail,
        String customerName,
        BigDecimal totalAmount,
        String currency,
        String paymentMethod,
        String paymentStatus,
        String razorpayPaymentId,
        String razorpayOrderId,
        String razorpaySignature,
        List<OrderItemRequest> items,
        Instant createdAt) {
}
