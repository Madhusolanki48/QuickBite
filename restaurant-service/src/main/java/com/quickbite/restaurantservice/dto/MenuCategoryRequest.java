package com.quickbite.restaurantservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating or updating a MenuCategory.
 * All fields except {@code name} are optional on update.
 */
public record MenuCategoryRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 10) String icon,
        Integer displayOrder,
        Boolean active) {
}
