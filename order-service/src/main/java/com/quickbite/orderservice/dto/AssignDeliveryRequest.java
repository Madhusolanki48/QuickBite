package com.quickbite.orderservice.dto;

import jakarta.validation.constraints.NotNull;

public record AssignDeliveryRequest(
        @NotNull(message = "riderId is required") Long riderId
) {
}
