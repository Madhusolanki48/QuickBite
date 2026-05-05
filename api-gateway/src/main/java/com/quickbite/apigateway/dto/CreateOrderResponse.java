package com.quickbite.apigateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;

public record CreateOrderResponse(
        @JsonProperty("orderId")
        String orderId,
        @JsonProperty("razorpayOrderId")
        String razorpayOrderId,
        BigDecimal amount,
        String currency,
        String keyId,
        String receipt) {
}
