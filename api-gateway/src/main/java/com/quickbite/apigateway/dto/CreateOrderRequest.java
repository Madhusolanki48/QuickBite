package com.quickbite.apigateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateOrderRequest(
        // Amount is expected in the smallest currency unit (paise for INR).
        @NotNull @Positive BigDecimal amount,
        String currency,
        String receipt,
        @JsonProperty("notes")
        @JsonAlias({"note"})
        Map<String, Object> notes,
        Long customerId,
        Long restaurantId,
        String customerEmail,
        String customerName) {
}
