package com.quickbite.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull Long customerId,
        @NotNull Long restaurantId,
        @NotBlank String customerEmail,
        @NotEmpty List<OrderItemRequest> items,
        String customerName,
        String customerPhone,
        String deliveryAddress,
        String note,
        String promoCode,
        Double discountAmount,
        String paymentMethod,
        String paymentStatus,
        String razorpayPaymentId,
        String razorpayOrderId,
        String razorpaySignature) {

    public CreateOrderRequest(
            Long customerId,
            Long restaurantId,
            String customerEmail,
            List<OrderItemRequest> items,
            String promoCode,
            Double discountAmount,
            String paymentMethod,
            String paymentStatus,
            String razorpayPaymentId,
            String razorpayOrderId,
            String razorpaySignature) {
        this(customerId, restaurantId, customerEmail, items, null, null, null, null,
                promoCode, discountAmount, paymentMethod, paymentStatus, razorpayPaymentId, razorpayOrderId, razorpaySignature);
    }
}
