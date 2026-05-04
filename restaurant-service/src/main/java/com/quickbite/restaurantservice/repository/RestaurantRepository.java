package com.quickbite.restaurantservice.repository;

import com.quickbite.restaurantservice.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByOwnerEmailIgnoreCase(String ownerEmail);

    List<Restaurant> findByOwnerId(Long ownerId);
}
