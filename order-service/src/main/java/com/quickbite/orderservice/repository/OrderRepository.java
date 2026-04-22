package com.quickbite.orderservice.repository;

import com.quickbite.orderservice.model.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<FoodOrder, Long> {
}
