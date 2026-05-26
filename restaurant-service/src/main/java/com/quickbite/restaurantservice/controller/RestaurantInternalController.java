package com.quickbite.restaurantservice.controller;

import com.quickbite.restaurantservice.dto.internal.ItemValidationResponse;
import com.quickbite.restaurantservice.dto.internal.ValidateItemsRequest;
import com.quickbite.restaurantservice.dto.internal.ValidatedItemDto;
import com.quickbite.restaurantservice.exception.NotFoundException;
import com.quickbite.restaurantservice.model.MenuItem;
import com.quickbite.restaurantservice.model.MenuItemAvailability;
import com.quickbite.restaurantservice.model.Restaurant;
import com.quickbite.restaurantservice.model.RestaurantStatus;
import com.quickbite.restaurantservice.repository.MenuItemRepository;
import com.quickbite.restaurantservice.repository.RestaurantRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants/internal")
@RequiredArgsConstructor
public class RestaurantInternalController {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final com.quickbite.restaurantservice.service.RestaurantService restaurantService;

    @GetMapping("/{restaurantId}")
    public ResponseEntity<com.quickbite.restaurantservice.dto.RestaurantResponse> getRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantService.findById(restaurantId));
    }

    @PostMapping("/validate-items")
    public ResponseEntity<ItemValidationResponse> validateItems(@Valid @RequestBody ValidateItemsRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new NotFoundException("Restaurant not found with id: " + request.restaurantId()));

        boolean isOpen = restaurant.getStatus() == RestaurantStatus.ACTIVE;
        List<String> errors = new ArrayList<>();
        if (!isOpen) {
            errors.add("Restaurant '" + restaurant.getName() + "' is currently closed");
        }

        List<ValidatedItemDto> validatedItems = new ArrayList<>();
        for (Long itemId : request.menuItemIds()) {
            MenuItem item = menuItemRepository.findById(itemId).orElse(null);
            if (item == null) {
                errors.add("Menu item with id " + itemId + " does not exist");
                continue;
            }

            if (!item.getRestaurant().getId().equals(restaurant.getId())) {
                errors.add("Menu item '" + item.getName() + "' (id: " + itemId + ") does not belong to restaurant '"
                        + restaurant.getName() + "'");
            }

            boolean isAvailable = item.getAvailability() == null || item.getAvailability() == MenuItemAvailability.AVAILABLE;
            if (!isAvailable) {
                errors.add("Menu item '" + item.getName() + "' is currently unavailable");
            }

            boolean isCategoryActive = item.getCategory() == null || item.getCategory().isActive();
            if (!isCategoryActive) {
                errors.add("Category for menu item '" + item.getName() + "' is currently inactive");
            }

            double price = item.getPrice();
            Integer discount = item.getDiscountPercent();
            double effectivePrice = (discount != null && discount > 0)
                    ? Math.max(0.0, price * (1.0 - (discount / 100.0)))
                    : price;

            validatedItems.add(new ValidatedItemDto(
                    item.getId(),
                    item.getName(),
                    item.getCategory() != null ? item.getCategory().getId() : null,
                    item.getCategory() != null ? item.getCategory().getName() : "Uncategorized",
                    isCategoryActive,
                    isAvailable,
                    price,
                    discount,
                    effectivePrice,
                    item.getImageUrl()
            ));
        }

        return ResponseEntity.ok(new ItemValidationResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getStatus().name(),
                isOpen,
                validatedItems,
                errors
        ));
    }

    @GetMapping("/{restaurantId}/menu-items/{menuItemId}")
    public ResponseEntity<ValidatedItemDto> getValidatedMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found with id: " + restaurantId));

        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new NotFoundException("Menu item not found with id: " + menuItemId));

        if (!item.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("Menu item " + menuItemId + " does not belong to restaurant " + restaurantId);
        }

        boolean isCategoryActive = item.getCategory() == null || item.getCategory().isActive();
        boolean isAvailable = item.getAvailability() == null || item.getAvailability() == MenuItemAvailability.AVAILABLE;
        double price = item.getPrice();
        Integer discount = item.getDiscountPercent();
        double effectivePrice = (discount != null && discount > 0)
                ? Math.max(0.0, price * (1.0 - (discount / 100.0)))
                : price;

        return ResponseEntity.ok(new ValidatedItemDto(
                item.getId(),
                item.getName(),
                item.getCategory() != null ? item.getCategory().getId() : null,
                item.getCategory() != null ? item.getCategory().getName() : "Uncategorized",
                isCategoryActive,
                isAvailable,
                price,
                discount,
                effectivePrice,
                item.getImageUrl()
        ));
    }
}
