package com.quickbite.apigateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VerifyPaymentResponse(
        @JsonProperty("success")
        boolean verified,
        String message,
        String razorpayOrderId,
        String razorpayPaymentId,
        String razorpaySignature) {
}
