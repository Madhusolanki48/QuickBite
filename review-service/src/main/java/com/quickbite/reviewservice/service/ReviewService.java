package com.quickbite.reviewservice.service;

import com.quickbite.reviewservice.dto.ReviewRequest;
import com.quickbite.reviewservice.dto.ReviewResponse;
import com.quickbite.reviewservice.model.Review;
import com.quickbite.reviewservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public List<ReviewResponse> all() {
        return reviewRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<ReviewResponse> forCustomer(String email) {
        return reviewRepository.findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(email).stream().map(this::toResponse).toList();
    }

    public List<ReviewResponse> forRestaurant(Long restaurantId) {
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream().map(this::toResponse).toList();
    }

    public ReviewResponse create(ReviewRequest request) {
        Review saved = reviewRepository.save(Review.builder()
                .orderId(request.orderId())
                .customerId(request.customerId())
                .customerEmail(request.customerEmail().trim().toLowerCase())
                .restaurantId(request.restaurantId())
                .restaurantName(request.restaurantName().trim())
                .deliveryAgentName(request.deliveryAgentName() == null || request.deliveryAgentName().isBlank() ? null : request.deliveryAgentName().trim())
                .restaurantRating(request.restaurantRating())
                .deliveryRating(request.deliveryRating())
                .comment(request.comment() == null ? "" : request.comment().trim())
                .build());
        return toResponse(saved);
    }

    public void delete(Long id) {
        reviewRepository.deleteById(id);
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getOrderId(),
                review.getCustomerId(),
                review.getCustomerEmail(),
                review.getRestaurantId(),
                review.getRestaurantName(),
                review.getDeliveryAgentName(),
                review.getRestaurantRating(),
                review.getDeliveryRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
