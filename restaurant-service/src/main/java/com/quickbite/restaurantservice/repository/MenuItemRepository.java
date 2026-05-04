package com.quickbite.restaurantservice.repository;

import com.quickbite.restaurantservice.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
}
