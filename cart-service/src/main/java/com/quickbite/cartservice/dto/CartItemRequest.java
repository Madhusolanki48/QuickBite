package com.quickbite.cartservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemRequest(
                @NotNull Long customerId,
                @NotNull Long restaurantId,
                @NotBlank String restaurantName,
                @NotNull Long menuItemId,
                @NotBlank String itemName,
                @Positive double unitPrice,
                @Min(1) int quantity,
                String imageUrl,
                String category) {
}
