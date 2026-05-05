package com.quickbite.apigateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderItemRequest(
        Long menuItemId,
        @NotBlank String itemName,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive BigDecimal unitPrice) {
}
