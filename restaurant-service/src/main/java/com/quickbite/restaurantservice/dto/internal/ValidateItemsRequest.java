package com.quickbite.restaurantservice.dto.internal;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ValidateItemsRequest(
        @NotNull Long restaurantId,
        @NotEmpty List<Long> menuItemIds
) {}
