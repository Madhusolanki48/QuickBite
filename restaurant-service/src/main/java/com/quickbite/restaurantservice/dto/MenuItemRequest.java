package com.quickbite.restaurantservice.dto;

import jakarta.validation.constraints.*;

public record MenuItemRequest(
                @NotBlank String name,
                String description,
                @Positive double price) {
}
