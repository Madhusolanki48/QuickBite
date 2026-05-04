package com.quickbite.restaurantservice.controller;

import com.quickbite.restaurantservice.dto.*;
import com.quickbite.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> all() {
        return ResponseEntity.ok(restaurantService.findAll());
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<RestaurantResponse>> byOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(restaurantService.findByOwnerId(ownerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> one(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.findById(id));
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> create(@Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.create(request));
    }

    @PostMapping("/{id}/menu-items")
    public ResponseEntity<RestaurantResponse> addMenuItem(@PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(restaurantService.addMenuItem(id, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> update(@PathVariable Long id,
            @RequestBody RestaurantUpdateRequest request) {
        return ResponseEntity.ok(restaurantService.update(id, request));
    }

    @PutMapping("/{restaurantId}/menu-items/{menuItemId}")
    public ResponseEntity<RestaurantResponse> updateMenuItem(@PathVariable Long restaurantId,
            @PathVariable Long menuItemId, @RequestBody MenuItemUpdateRequest request) {
        return ResponseEntity.ok(restaurantService.updateMenuItem(restaurantId, menuItemId, request));
    }

    @DeleteMapping("/{restaurantId}/menu-items/{menuItemId}")
    public ResponseEntity<RestaurantResponse> deleteMenuItem(@PathVariable Long restaurantId,
            @PathVariable Long menuItemId) {
        return ResponseEntity.ok(restaurantService.removeMenuItem(restaurantId, menuItemId));
    }
}
