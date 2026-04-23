package com.quickbite.cartservice.dto;

import java.time.Instant;

public record CartItemResponse(
                Long id,
                Long customerId,
                Long restaurantId,
                String restaurantName,
                Long menuItemId,
                String itemName,
                double unitPrice,
                int quantity,
                String imageUrl,
                String category,
                Instant createdAt) {
}
