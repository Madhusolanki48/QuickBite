package com.quickbite.orderservice.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemValidationResponse(
        Long restaurantId,
        String restaurantName,
        String restaurantStatus,
        Boolean restaurantOpen,
        List<ValidatedItemDto> items,
        List<String> errors,
        String message
) {
    public ItemValidationResponse(boolean valid, Long restaurantId, String restaurantName, List<ValidatedItemDto> items, String message) {
        this(
            restaurantId,
            restaurantName,
            valid ? "ACTIVE" : "INACTIVE",
            valid,
            items,
            valid ? List.of() : (message != null ? List.of(message) : List.of("Invalid items")),
            message
        );
    }

    public boolean valid() {
        return (errors == null || errors.isEmpty()) && (items != null && !items.isEmpty());
    }

    public String message() {
        if (message != null && !message.isBlank()) {
            return message;
        }
        return (errors != null && !errors.isEmpty()) ? String.join("; ", errors) : null;
    }
}

