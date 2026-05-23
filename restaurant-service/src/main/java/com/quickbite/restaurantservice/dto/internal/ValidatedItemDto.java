package com.quickbite.restaurantservice.dto.internal;

public record ValidatedItemDto(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        boolean categoryActive,
        boolean available,
        double price,
        Integer discountPercent,
        double effectivePrice,
        String imageUrl
) {}
