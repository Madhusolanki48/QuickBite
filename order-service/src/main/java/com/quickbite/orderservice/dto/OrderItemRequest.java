package com.quickbite.orderservice.dto;

import jakarta.validation.constraints.*;

public record OrderItemRequest(@NotNull Long menuItemId, @NotBlank String itemName, @Positive int quantity,
        @Positive double unitPrice) {
}
