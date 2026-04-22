package com.quickbite.restaurantservice.dto;

import com.quickbite.restaurantservice.model.MenuItemAvailability;

public record MenuItemUpdateRequest(
                String name,
                String description,
                Double price,
                MenuItemAvailability availability) {
}
