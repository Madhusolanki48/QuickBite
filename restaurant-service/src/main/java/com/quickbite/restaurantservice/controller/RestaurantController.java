package com.quickbite.restaurantservice.controller;

import com.quickbite.restaurantservice.dto.*;
import com.quickbite.restaurantservice.service.OperatingHourService;
import com.quickbite.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final OperatingHourService operatingHourService;

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> all() {
        return ResponseEntity.ok(restaurantService.findAll());
    }

    @GetMapping("/owner/me")
    public ResponseEntity<RestaurantResponse> ownerMe(Authentication auth) {
        return ResponseEntity.ok(restaurantService.findByOwnerMe(auth));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<RestaurantResponse>> byOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(restaurantService.findByOwnerId(ownerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> one(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.findById(id));
    }

    @GetMapping("/{id}/hours")
    public ResponseEntity<List<OperatingHourResponse>> getOperatingHours(@PathVariable Long id) {
        return ResponseEntity.ok(operatingHourService.getSchedule(id));
    }

    @PutMapping("/{id}/hours")
    public ResponseEntity<List<OperatingHourResponse>> updateOperatingHours(
            @PathVariable Long id,
            @RequestBody List<OperatingHourRequest> requests,
            Authentication auth) {
        return ResponseEntity.ok(operatingHourService.saveSchedule(id, requests, auth));
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> create(@Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.create(request));
    }

    /**
     * Add a menu item to a restaurant's menu.
     * Authentication is required; the service enforces that the caller owns the restaurant
     * (or is ADMIN).
     */
    @PostMapping("/{id}/menu-items")
    public ResponseEntity<RestaurantResponse> addMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request,
            Authentication auth) {
        return ResponseEntity.ok(restaurantService.addMenuItem(id, request, auth));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> update(
            @PathVariable Long id,
            @RequestBody RestaurantUpdateRequest request,
            Authentication auth) {
        return ResponseEntity.ok(restaurantService.update(id, request, auth));
    }

    /**
     * Update a specific menu item. Enforces ownership.
     */
    @PutMapping("/{restaurantId}/menu-items/{menuItemId}")
    public ResponseEntity<RestaurantResponse> updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            @RequestBody MenuItemUpdateRequest request,
            Authentication auth) {
        return ResponseEntity.ok(restaurantService.updateMenuItem(restaurantId, menuItemId, request, auth));
    }

    /**
     * Delete a specific menu item. Enforces ownership.
     */
    @DeleteMapping("/{restaurantId}/menu-items/{menuItemId}")
    public ResponseEntity<RestaurantResponse> deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            Authentication auth) {
        return ResponseEntity.ok(restaurantService.removeMenuItem(restaurantId, menuItemId, auth));
    }
}
