package com.quickbite.restaurantservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a new menu item.
 *
 * categoryId: optional — if null, the item is assigned to the restaurant's "Uncategorized" category.
 * isVeg: optional — defaults to true if null.
 * displayOrder: optional — defaults to 0 if null.
 */
public record MenuItemRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        @Positive double price,
        Long categoryId,
        Boolean isVeg,
        @Size(max = 500) String imageUrl,
        @Size(max = 10) String icon,
        Integer discountPercent,
        Integer prepTimeMinutes,
        Integer displayOrder) {

    public MenuItemRequest(String name, String description, double price) {
        this(name, description, price, null, true, null, null, null, null, 0);
    }
}
