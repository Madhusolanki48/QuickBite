package com.quickbite.restaurantservice.dto;

import com.quickbite.restaurantservice.model.MenuItemAvailability;

public record MenuItemResponse(Long id, String name, String description, double price,
        MenuItemAvailability availability) {
}
