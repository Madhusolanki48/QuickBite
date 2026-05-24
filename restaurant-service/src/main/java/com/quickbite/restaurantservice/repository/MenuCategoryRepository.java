package com.quickbite.restaurantservice.repository;

import com.quickbite.restaurantservice.model.MenuCategory;
import com.quickbite.restaurantservice.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

    /** All categories for a restaurant, ordered by displayOrder ascending. */
    List<MenuCategory> findByRestaurantOrderByDisplayOrderAsc(Restaurant restaurant);

    /** Look up a category by restaurant and slug (for deduplication and migration). */
    Optional<MenuCategory> findByRestaurantAndSlug(Restaurant restaurant, String slug);

    /** Validate that a category belongs to a specific restaurant (ownership guard). */
    Optional<MenuCategory> findByIdAndRestaurant(Long id, Restaurant restaurant);

    /** All categories for a restaurant by ID (used for displayOrder calculation). */
    List<MenuCategory> findByRestaurantId(Long restaurantId);

    /** Count how many menu items reference this category (to block deletion when non-empty). */
    long countByRestaurantId(Long restaurantId);
}
