package com.quickbite.restaurantservice.dto;

import com.quickbite.restaurantservice.model.MenuItemAvailability;
import jakarta.validation.constraints.Size;

/** All fields are optional — only non-null values are applied as patches. */
public record MenuItemUpdateRequest(
        @Size(max = 120) String name,
        @Size(max = 500) String description,
        Double price,
        MenuItemAvailability availability,
        Long categoryId,
        Boolean isVeg,
        @Size(max = 500) String imageUrl,
        @Size(max = 10) String icon,
        Integer discountPercent,
        Integer prepTimeMinutes,
        Integer displayOrder) {
}
