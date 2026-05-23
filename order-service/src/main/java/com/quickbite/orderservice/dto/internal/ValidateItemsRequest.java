package com.quickbite.orderservice.dto.internal;

import java.util.List;

public record ValidateItemsRequest(
        Long restaurantId,
        List<Long> menuItemIds
) {}
