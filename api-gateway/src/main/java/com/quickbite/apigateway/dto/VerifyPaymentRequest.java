package com.quickbite.apigateway.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VerifyPaymentRequest(
        @NotBlank
        @JsonProperty("razorpay_order_id")
        @JsonAlias({"razorpayOrderId"})
        String razorpayOrderId,
        @NotBlank
        @JsonProperty("razorpay_payment_id")
        @JsonAlias({"razorpayPaymentId"})
        String razorpayPaymentId,
        @NotBlank
        @JsonProperty("razorpay_signature")
        @JsonAlias({"razorpaySignature"})
        String razorpaySignature) {
}
