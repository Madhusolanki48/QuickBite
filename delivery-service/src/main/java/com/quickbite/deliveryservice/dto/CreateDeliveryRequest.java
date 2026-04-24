package com.quickbite.deliveryservice.dto;

import jakarta.validation.constraints.*;

public record CreateDeliveryRequest(@NotNull Long orderId, @NotNull Long riderId, @NotBlank String riderName,
        @NotBlank String riderPhone, @NotBlank String deliveryAddress) {
}
