package com.quickbite.restaurantservice.service;

import com.quickbite.restaurantservice.model.MenuCategory;
import com.quickbite.restaurantservice.model.MenuItem;
import com.quickbite.restaurantservice.model.Restaurant;
import com.quickbite.restaurantservice.repository.MenuCategoryRepository;
import com.quickbite.restaurantservice.repository.MenuItemRepository;
import com.quickbite.restaurantservice.repository.OperatingHourRepository;
import com.quickbite.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Runs once on application startup (after Spring context is fully ready) to bring
 * existing database rows in line with the new schema requirements.
 *
 * Migration 1 — isVeg:
 *   MenuItem rows that have is_veg IS NULL (pre-migration rows) are set to TRUE (vegetarian).
 *   This is the safe conservative default for existing data. The migration logs exactly
 *   how many rows were updated so operators can review and correct non-veg items manually
 *   via the API if needed.
 *
 * Migration 2 — category:
 *   MenuItem rows with category_id IS NULL are assigned to a per-restaurant "Uncategorized"
 *   category (created on demand). This satisfies the requirement that every MenuItem belongs
 *   to exactly one MenuCategory after migration. The migration logs how many items per
 *   restaurant were assigned so operators know which categories to reorganise.
 *
 * This service is idempotent — running it multiple times has no additional effect once
 * all rows have been migrated.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataMigrationService {

    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final OperatingHourRepository operatingHourRepository;
    private final OperatingHourService operatingHourService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void runMigrations() {
        log.info("[Migration] Starting data migrations...");
        migrateIsVeg();
        migrateMenuItemCategories();
        migrateOperatingHours();
        log.info("[Migration] Data migrations complete.");
    }

    // ── Migration 1: isVeg ────────────────────────────────────────────────────

    private void migrateIsVeg() {
        List<MenuItem> nullVegItems = menuItemRepository.findByIsVegIsNull();
        if (nullVegItems.isEmpty()) {
            log.info("[Migration] isVeg: All menu items already have a value — no changes needed.");
            return;
        }

        nullVegItems.forEach(item -> item.setIsVeg(true));
        menuItemRepository.saveAll(nullVegItems);

        log.warn("[Migration] isVeg: Set is_veg=TRUE for {} existing menu item(s) that had NULL. "
                + "These items were assumed vegetarian (safe default). "
                + "Review non-veg items via PUT /api/restaurants/{id}/menu-items/{itemId} and "
                + "set isVeg=false where needed.",
                nullVegItems.size());

        nullVegItems.forEach(item ->
                log.info("[Migration] isVeg: item id={} name='{}' (restaurant_id={}) → isVeg=true",
                        item.getId(), item.getName(), item.getRestaurant().getId()));
    }

    // ── Migration 2: category ─────────────────────────────────────────────────

    private void migrateMenuItemCategories() {
        List<MenuItem> uncategorizedItems = menuItemRepository.findByCategoryIsNull();
        if (uncategorizedItems.isEmpty()) {
            log.info("[Migration] category: All menu items already have a category — no changes needed.");
            return;
        }

        // Group by restaurant to create/reuse one "Uncategorized" category per restaurant
        List<Restaurant> restaurants = restaurantRepository.findAll();
        int totalMigrated = 0;

        for (Restaurant restaurant : restaurants) {
            List<MenuItem> restaurantUncategorized = uncategorizedItems.stream()
                    .filter(i -> restaurant.getId().equals(i.getRestaurant().getId()))
                    .toList();

            if (restaurantUncategorized.isEmpty()) continue;

            // Find or create the "Uncategorized" category for this restaurant
            MenuCategory defaultCat = menuCategoryRepository
                    .findByRestaurantAndSlug(restaurant, "uncategorized")
                    .orElseGet(() -> {
                        MenuCategory cat = MenuCategory.builder()
                                .restaurant(restaurant)
                                .name("Uncategorized")
                                .slug("uncategorized")
                                .displayOrder(999)
                                .active(true)
                                .build();
                        MenuCategory saved = menuCategoryRepository.save(cat);
                        log.info("[Migration] category: Created 'Uncategorized' category (id={}) "
                                + "for restaurant '{}' (id={})",
                                saved.getId(), restaurant.getName(), restaurant.getId());
                        return saved;
                    });

            restaurantUncategorized.forEach(item -> item.setCategory(defaultCat));
            menuItemRepository.saveAll(restaurantUncategorized);

            log.warn("[Migration] category: Assigned {} menu item(s) in restaurant '{}' (id={}) "
                    + "to 'Uncategorized' category (id={}). "
                    + "Use POST /api/restaurants/{}/categories to create real categories, "
                    + "then PUT /api/restaurants/{}/menu-items/{itemId} to reassign.",
                    restaurantUncategorized.size(), restaurant.getName(), restaurant.getId(),
                    defaultCat.getId(), restaurant.getId(), restaurant.getId());

            restaurantUncategorized.forEach(item ->
                    log.info("[Migration] category: item id={} name='{}' → category='Uncategorized' (id={})",
                            item.getId(), item.getName(), defaultCat.getId()));

            totalMigrated += restaurantUncategorized.size();
        }

        log.info("[Migration] category: Total {} menu item(s) assigned to 'Uncategorized' categories.",
                totalMigrated);
    }

    // ── Migration 3: operatingHours ───────────────────────────────────────────

    private void migrateOperatingHours() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        int seededCount = 0;
        for (Restaurant restaurant : restaurants) {
            if (!operatingHourRepository.existsByRestaurant(restaurant)) {
                operatingHourService.seedDefaultsIfEmpty(restaurant);
                seededCount++;
                log.info("[Migration] operatingHours: Seeded 7-day default operating hours for restaurant '{}' (id={})",
                        restaurant.getName(), restaurant.getId());
            }
        }
        if (seededCount == 0) {
            log.info("[Migration] operatingHours: All restaurants already have operating hours — no changes needed.");
        } else {
            log.info("[Migration] operatingHours: Seeded default operating hours for {} restaurant(s).", seededCount);
        }
    }
}
