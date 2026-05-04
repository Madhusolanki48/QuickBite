package com.quickbite.restaurantservice.dto;

import com.quickbite.restaurantservice.model.*;
import java.time.Instant;
import java.util.List;

public record RestaurantResponse(Long id, String name, String address, String phoneNumber, String email,
        CuisineType cuisineType, RestaurantStatus status, double rating, Instant createdAt, Long ownerId,
        String ownerEmail, String ownerName, List<MenuItemResponse> menuItems) {
}
