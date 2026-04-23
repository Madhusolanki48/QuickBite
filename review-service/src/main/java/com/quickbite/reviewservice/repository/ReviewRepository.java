package com.quickbite.reviewservice.repository;

import com.quickbite.reviewservice.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(String customerEmail);

    List<Review> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
}
