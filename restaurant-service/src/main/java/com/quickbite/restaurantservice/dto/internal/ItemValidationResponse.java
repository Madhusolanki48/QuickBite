package com.quickbite.restaurantservice.dto.internal;

import java.util.List;

public record ItemValidationResponse(
        Long restaurantId,
        String restaurantName,
        String restaurantStatus,
        boolean restaurantOpen,
        List<ValidatedItemDto> items,
        List<String> errors
) {}
