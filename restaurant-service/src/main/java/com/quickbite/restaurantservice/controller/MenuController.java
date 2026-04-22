package com.quickbite.restaurantservice.controller;

import com.quickbite.restaurantservice.dto.MenuItemRequest;
import com.quickbite.restaurantservice.dto.MenuItemResponse;
import com.quickbite.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {
    private final RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> all() {
        return ResponseEntity.ok(restaurantService.findAllMenuItems());
    }

    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<List<MenuItemResponse>> byRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantService.findMenuItemsByRestaurantId(restaurantId));
    }

    @PostMapping("/restaurants/{restaurantId}")
    public ResponseEntity<?> add(@PathVariable Long restaurantId, @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(restaurantService.addMenuItem(restaurantId, request));
    }
}
