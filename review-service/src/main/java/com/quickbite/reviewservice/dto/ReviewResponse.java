package com.quickbite.reviewservice.dto;

import java.time.Instant;

public record ReviewResponse(
                Long id,
                Long orderId,
                Long customerId,
                String customerEmail,
                Long restaurantId,
                String restaurantName,
                String deliveryAgentName,
                int restaurantRating,
                int deliveryRating,
                String comment,
                Instant createdAt) {
}
