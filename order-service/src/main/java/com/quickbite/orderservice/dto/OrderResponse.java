package com.quickbite.orderservice.dto;

import com.quickbite.orderservice.model.*;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        Long restaurantId,
        String customerEmail,
        double totalAmount,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        Instant createdAt,
        List<OrderItemResponse> items,
        Double deliveryFee,
        Double gst,
        Double discountAmount,
        Double subtotal,
        String customerName,
        String customerPhone,
        String deliveryAddress,
        String note,
        Long deliveryAgentId,
        String deliveryAgentName,
        String deliveryAgentEmail,
        String deliveryAgentPhone,
        DeliveryStatus deliveryAgentStatus) {

    public OrderResponse(Long id, Long customerId, Long restaurantId, String customerEmail, double totalAmount,
            OrderStatus orderStatus, PaymentStatus paymentStatus, Instant createdAt, List<OrderItemResponse> items) {
        this(id, customerId, restaurantId, customerEmail, totalAmount, orderStatus, paymentStatus, createdAt, items,
                0.0, 0.0, 0.0, totalAmount, null, null, null, null, null, null, null, null, DeliveryStatus.UNASSIGNED);
    }
}
