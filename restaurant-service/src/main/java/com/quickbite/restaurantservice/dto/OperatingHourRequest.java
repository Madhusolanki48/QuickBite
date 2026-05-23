package com.quickbite.restaurantservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for a single operating-hour row.
 * The frontend sends all 7 days in a list via PUT /api/restaurants/{id}/hours.
 */
public record OperatingHourRequest(
        @NotBlank String day,
        @NotBlank @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "openTime must be HH:mm") String openTime,
        @NotBlank @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "closeTime must be HH:mm") String closeTime,
        boolean openToday) {
}
