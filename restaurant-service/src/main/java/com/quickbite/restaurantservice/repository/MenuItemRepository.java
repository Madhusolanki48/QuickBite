package com.quickbite.restaurantservice.repository;

import com.quickbite.restaurantservice.model.MenuItem;
import com.quickbite.restaurantservice.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /**
     * Find all menu items where isVeg is null.
     * Used by DataMigrationService to set a safe default (true) for existing rows.
     */
    @Query("SELECT m FROM MenuItem m WHERE m.isVeg IS NULL")
    List<MenuItem> findByIsVegIsNull();

    /**
     * Find all menu items where category is null.
     * Used by DataMigrationService to assign existing items to "Uncategorized".
     */
    @Query("SELECT m FROM MenuItem m WHERE m.category IS NULL")
    List<MenuItem> findByCategoryIsNull();

    /** Find all menu items for a specific restaurant. */
    List<MenuItem> findByRestaurant(Restaurant restaurant);

    /** Find menu items for a restaurant, ordered by category displayOrder then item displayOrder. */
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant = :restaurant ORDER BY m.displayOrder ASC")
    List<MenuItem> findByRestaurantOrderByDisplayOrder(Restaurant restaurant);
}
