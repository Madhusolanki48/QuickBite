package com.quickbite.restaurantservice.dto;

import com.quickbite.restaurantservice.model.MenuItemAvailability;

/**
 * Response body for a single menu item.
 * Includes category info (id, name, slug) so the frontend can group items without guessing.
 * isVeg reflects the explicit stored value — never null after migration.
 */
public record MenuItemResponse(
        Long id,
        String name,
        String description,
        double price,
        MenuItemAvailability availability,
        Long categoryId,
        String categoryName,
        String categorySlug,
        Boolean isVeg,
        String imageUrl,
        String icon,
        Integer discountPercent,
        Integer prepTimeMinutes,
        int displayOrder) {
}
