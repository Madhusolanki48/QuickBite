package com.quickbite.restaurantservice.controller;

import com.quickbite.restaurantservice.dto.MenuCategoryRequest;
import com.quickbite.restaurantservice.dto.MenuCategoryResponse;
import com.quickbite.restaurantservice.service.MenuCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for per-restaurant menu category management.
 *
 * All write operations enforce restaurant ownership (see MenuCategoryService.assertOwnership).
 * Admins may operate across all restaurants.
 */
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/categories")
@RequiredArgsConstructor
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;

    /** List all categories for a restaurant, ordered by displayOrder ASC. */
    @GetMapping
    public ResponseEntity<List<MenuCategoryResponse>> list(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuCategoryService.listForRestaurant(restaurantId));
    }

    /**
     * Create a new category for a restaurant.
     * Requires: caller is the restaurant owner (ownerEmail matches JWT) or ADMIN.
     */
    @PostMapping
    public ResponseEntity<MenuCategoryResponse> create(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuCategoryRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuCategoryService.create(restaurantId, request, auth));
    }

    /**
     * Update an existing category.
     * Requires: caller is the restaurant owner or ADMIN.
     */
    @PutMapping("/{categoryId}")
    public ResponseEntity<MenuCategoryResponse> update(
            @PathVariable Long restaurantId,
            @PathVariable Long categoryId,
            @Valid @RequestBody MenuCategoryRequest request,
            Authentication auth) {
        return ResponseEntity.ok(menuCategoryService.update(restaurantId, categoryId, request, auth));
    }

    /**
     * Delete a category. Fails with 409 Conflict if the category still has menu items.
     * Requires: caller is the restaurant owner or ADMIN.
     */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long restaurantId,
            @PathVariable Long categoryId,
            Authentication auth) {
        menuCategoryService.delete(restaurantId, categoryId, auth);
        return ResponseEntity.noContent().build();
    }
}
