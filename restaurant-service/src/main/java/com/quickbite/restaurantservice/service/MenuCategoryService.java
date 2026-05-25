package com.quickbite.restaurantservice.service;

import com.quickbite.restaurantservice.dto.MenuCategoryRequest;
import com.quickbite.restaurantservice.dto.MenuCategoryResponse;
import com.quickbite.restaurantservice.exception.ForbiddenException;
import com.quickbite.restaurantservice.exception.NotFoundException;
import com.quickbite.restaurantservice.model.MenuCategory;
import com.quickbite.restaurantservice.model.Restaurant;
import com.quickbite.restaurantservice.repository.MenuCategoryRepository;
import com.quickbite.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing per-restaurant MenuCategory entities.
 *
 * Ownership rules:
 *   - RESTAURANT_OWNER may only create/update/delete categories for their own restaurant
 *     (matched via restaurant.ownerEmail == JWT subject email).
 *   - ADMIN may operate across all restaurants.
 *   - Read operations (GET) are permitted to any authenticated user.
 */
@Service
@RequiredArgsConstructor
public class MenuCategoryService {

    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository menuCategoryRepository;

    // ── Read ─────────────────────────────────────────────────────────────────

    @Cacheable(cacheNames = "menu-categories", key = "#restaurantId")
    public List<MenuCategoryResponse> listForRestaurant(Long restaurantId) {
        Restaurant restaurant = getRestaurant(restaurantId);
        return menuCategoryRepository.findByRestaurantOrderByDisplayOrderAsc(restaurant)
                .stream().map(c -> toResponse(c, restaurant)).toList();
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    @CacheEvict(cacheNames = {"menu-categories", "restaurants", "restaurant-by-id"}, allEntries = true)
    @Transactional
    public MenuCategoryResponse create(Long restaurantId, MenuCategoryRequest request,
            Authentication auth) {
        Restaurant restaurant = getRestaurant(restaurantId);
        assertOwnership(restaurant, auth);

        String slug = slugify(request.name());
        // Make slug unique within this restaurant by appending suffix if needed
        slug = ensureUniqueSlug(restaurant, slug);

        int order = request.displayOrder() != null
                ? request.displayOrder()
                : computeNextOrder(restaurantId);

        MenuCategory cat = MenuCategory.builder()
                .restaurant(restaurant)
                .name(request.name().trim())
                .slug(slug)
                .icon(request.icon())
                .displayOrder(order)
                .active(request.active() != null ? request.active() : true)
                .build();

        return toResponse(menuCategoryRepository.save(cat), restaurant);
    }

    @CacheEvict(cacheNames = {"menu-categories", "restaurants", "restaurant-by-id"}, allEntries = true)
    @Transactional
    public MenuCategoryResponse update(Long restaurantId, Long categoryId,
            MenuCategoryRequest request, Authentication auth) {
        Restaurant restaurant = getRestaurant(restaurantId);
        assertOwnership(restaurant, auth);

        MenuCategory cat = getOwnedCategory(categoryId, restaurant);

        if (request.name() != null && !request.name().isBlank()) {
            cat.setName(request.name().trim());
            cat.setSlug(slugify(request.name()));
        }
        if (request.icon() != null) cat.setIcon(request.icon().isBlank() ? null : request.icon());
        if (request.displayOrder() != null) cat.setDisplayOrder(request.displayOrder());
        if (request.active() != null) cat.setActive(request.active());

        return toResponse(menuCategoryRepository.save(cat), restaurant);
    }

    @CacheEvict(cacheNames = {"menu-categories", "restaurants", "restaurant-by-id"}, allEntries = true)
    @Transactional
    public void delete(Long restaurantId, Long categoryId, Authentication auth) {
        Restaurant restaurant = getRestaurant(restaurantId);
        assertOwnership(restaurant, auth);

        MenuCategory cat = getOwnedCategory(categoryId, restaurant);

        // Guard: do not delete a category that still has menu items assigned to it
        long itemCount = restaurant.getMenuItems().stream()
                .filter(i -> cat.equals(i.getCategory()))
                .count();
        if (itemCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete category '" + cat.getName() + "' — it still has "
                            + itemCount + " menu item(s). Re-assign or delete those items first.");
        }

        menuCategoryRepository.delete(cat);
    }

    // ── Helpers (package-private so RestaurantService can reuse them) ─────────

    /**
     * Resolves a categoryId to a MenuCategory, validating it belongs to the given restaurant.
     * Returns null if categoryId is null (caller decides the fallback).
     */
    MenuCategory resolveCategory(Long categoryId, Restaurant restaurant) {
        if (categoryId == null) return null;
        return menuCategoryRepository.findByIdAndRestaurant(categoryId, restaurant)
                .orElseThrow(() -> new NotFoundException(
                        "Category id=" + categoryId + " does not belong to restaurant id="
                                + restaurant.getId()));
    }

    /**
     * Finds or creates the "Uncategorized" catch-all category for a restaurant.
     * Used by DataMigrationService and addMenuItem when no categoryId is provided.
     */
    @Transactional
    public MenuCategory getOrCreateUncategorized(Restaurant restaurant) {
        return menuCategoryRepository
                .findByRestaurantAndSlug(restaurant, "uncategorized")
                .orElseGet(() -> menuCategoryRepository.save(
                        MenuCategory.builder()
                                .restaurant(restaurant)
                                .name("Uncategorized")
                                .slug("uncategorized")
                                .displayOrder(999)
                                .active(true)
                                .build()));
    }

    MenuCategoryResponse toResponse(MenuCategory cat, Restaurant restaurant) {
        int itemCount = (int) restaurant.getMenuItems().stream()
                .filter(i -> cat.equals(i.getCategory()))
                .count();
        return new MenuCategoryResponse(
                cat.getId(),
                restaurant.getId(),
                cat.getName(),
                cat.getSlug(),
                cat.getIcon(),
                cat.getDisplayOrder(),
                cat.isActive(),
                itemCount);
    }

    // ── Ownership validation ──────────────────────────────────────────────────

    /**
     * Asserts that the authenticated principal owns this restaurant OR is an ADMIN.
     *
     * @throws ForbiddenException if the principal does not own the restaurant
     */
    void assertOwnership(Restaurant restaurant, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException("Not authenticated");
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return;  // Admins can operate across all restaurants

        String principalEmail = auth.getName();
        if (restaurant.getOwnerEmail() == null
                || !restaurant.getOwnerEmail().equalsIgnoreCase(principalEmail)) {
            throw new ForbiddenException(
                    "Access denied: you do not own restaurant id=" + restaurant.getId());
        }
    }

    public MenuCategory getOrCreateUncategorizedCategory(Restaurant restaurant) {
        return getOrCreateUncategorized(restaurant);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private Restaurant getRestaurant(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Restaurant not found: id=" + id));
    }

    private MenuCategory getOwnedCategory(Long categoryId, Restaurant restaurant) {
        return menuCategoryRepository.findByIdAndRestaurant(categoryId, restaurant)
                .orElseThrow(() -> new NotFoundException(
                        "Category id=" + categoryId + " not found for restaurant id="
                                + restaurant.getId()));
    }

    private String slugify(String name) {
        return name.trim().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String ensureUniqueSlug(Restaurant restaurant, String baseSlug) {
        String slug = baseSlug;
        int suffix = 1;
        while (menuCategoryRepository.findByRestaurantAndSlug(restaurant, slug).isPresent()) {
            slug = baseSlug + "-" + suffix++;
        }
        return slug;
    }

    private int computeNextOrder(Long restaurantId) {
        return menuCategoryRepository.findByRestaurantId(restaurantId).stream()
                .mapToInt(MenuCategory::getDisplayOrder)
                .max()
                .orElse(-1) + 1;
    }
}
