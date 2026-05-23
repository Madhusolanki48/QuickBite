package com.quickbite.restaurantservice.dto;

import com.quickbite.restaurantservice.model.*;
import java.time.Instant;
import java.util.List;

/**
 * Full response for a restaurant, including its categories and flat list of menu items.
 * The {@code categories} field allows clients to render a structured category → items hierarchy.
 * The {@code menuItems} flat list is preserved for backward compatibility.
 */
public record RestaurantResponse(
        Long id,
        String name,
        String address,
        String phoneNumber,
        String email,
        CuisineType cuisineType,
        RestaurantStatus status,
        double rating,
        Instant createdAt,
        Long ownerId,
        String ownerEmail,
        String ownerName,
        String description,
        Integer minOrder,
        String gstin,
        String fssai,
        List<MenuCategoryResponse> categories,
        List<MenuItemResponse> menuItems,
        List<OperatingHourResponse> operatingHours) {
}
