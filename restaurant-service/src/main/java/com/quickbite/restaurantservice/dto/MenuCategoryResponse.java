package com.quickbite.restaurantservice.dto;

/**
 * Response body for a MenuCategory.
 * {@code itemCount} reflects how many menu items currently belong to this category.
 */
public record MenuCategoryResponse(
        Long id,
        Long restaurantId,
        String name,
        String slug,
        String icon,
        int displayOrder,
        boolean active,
        int itemCount) {
}
