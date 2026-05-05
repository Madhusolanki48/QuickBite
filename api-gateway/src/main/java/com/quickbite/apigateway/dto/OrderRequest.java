package com.quickbite.apigateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderRequest(
        Long customerId,
        Long restaurantId,
        String customerEmail,
        String customerName,
        String customerPhone,
        @NotNull @Positive BigDecimal totalAmount,
        String currency,
        @NotBlank
        @JsonProperty("payment_method")
        @JsonAlias({"paymentMethod"})
        String paymentMethod,
        @NotBlank
        @JsonProperty("payment_status")
        @JsonAlias({"paymentStatus"})
        String paymentStatus,
        @JsonProperty("razorpay_payment_id")
        @JsonAlias({"razorpayPaymentId"})
        String razorpayPaymentId,
        @JsonProperty("razorpay_order_id")
        @JsonAlias({"razorpayOrderId"})
        String razorpayOrderId,
        @JsonProperty("razorpay_signature")
        @JsonAlias({"razorpaySignature"})
        String razorpaySignature,
        @Valid List<OrderItemRequest> items) {
}
