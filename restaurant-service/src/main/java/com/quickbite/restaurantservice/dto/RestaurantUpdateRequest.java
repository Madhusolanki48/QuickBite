package com.quickbite.restaurantservice.dto;

import com.quickbite.restaurantservice.model.CuisineType;
import com.quickbite.restaurantservice.model.RestaurantStatus;

public record RestaurantUpdateRequest(
                String name,
                String address,
                String phoneNumber,
                String email,
                CuisineType cuisineType,
                RestaurantStatus status,
                Double rating,
                Long ownerId,
                String ownerEmail,
                String ownerName) {
}
