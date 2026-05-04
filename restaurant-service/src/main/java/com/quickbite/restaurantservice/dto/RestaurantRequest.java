package com.quickbite.restaurantservice.dto;

import com.quickbite.restaurantservice.model.CuisineType;
import jakarta.validation.constraints.*;
import java.util.List;

public record RestaurantRequest(
                @NotBlank String name,
                @NotBlank String address,
                @NotBlank String phoneNumber,
                @NotBlank @Email String email,
                @NotNull CuisineType cuisineType,
                Double rating,
                Long ownerId,
                String ownerEmail,
                String ownerName,
                List<MenuItemRequest> menuItems) {
}
