package com.quickbite.reviewservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
                @NotNull Long orderId,
                @NotNull Long customerId,
                @NotBlank String customerEmail,
                @NotNull Long restaurantId,
                @NotBlank String restaurantName,
                String deliveryAgentName,
                @Min(1) @Max(5) int restaurantRating,
                @Min(1) @Max(5) int deliveryRating,
                String comment) {
}
