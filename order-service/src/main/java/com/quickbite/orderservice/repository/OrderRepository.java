package com.quickbite.orderservice.repository;

import com.quickbite.orderservice.model.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<FoodOrder, Long> {
    List<FoodOrder> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
    List<FoodOrder> findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(String customerEmail);
    List<FoodOrder> findAllByOrderByCreatedAtDesc();
}
