package com.quickbite.cartservice.repository;

import com.quickbite.cartservice.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    Optional<CartItem> findByCustomerIdAndRestaurantIdAndMenuItemId(Long customerId, Long restaurantId,
            Long menuItemId);

    void deleteByCustomerId(Long customerId);
}
