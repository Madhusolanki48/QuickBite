package com.quickbite.deliveryservice.dto;

public record InternalDeliveryAssignmentRequest(
        Long orderId,
        Long riderId,
        String riderName,
        String riderPhone,
        String deliveryAddress
) {}
