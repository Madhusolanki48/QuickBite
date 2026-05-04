package com.quickbite.cartservice.service;

import com.quickbite.cartservice.dto.CartItemRequest;
import com.quickbite.cartservice.dto.CartItemResponse;
import com.quickbite.cartservice.dto.CartSummaryResponse;
import com.quickbite.cartservice.model.CartItem;
import com.quickbite.cartservice.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepository;

    public CartSummaryResponse cart(Long customerId) {
        List<CartItemResponse> items = cartItemRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toResponse).toList();
        double subtotal = items.stream().mapToDouble(item -> item.unitPrice() * item.quantity()).sum();
        return new CartSummaryResponse(items, subtotal);
    }

    public CartItemResponse add(CartItemRequest request) {
        CartItem item = cartItemRepository.findByCustomerIdAndRestaurantIdAndMenuItemId(request.customerId(), request.restaurantId(), request.menuItemId())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + request.quantity());
                    existing.setUnitPrice(request.unitPrice());
                    existing.setRestaurantName(request.restaurantName().trim());
                    existing.setItemName(request.itemName().trim());
                    existing.setImageUrl(request.imageUrl());
                    existing.setCategory(request.category());
                    return existing;
                })
                .orElseGet(() -> CartItem.builder()
                        .customerId(request.customerId())
                        .restaurantId(request.restaurantId())
                        .restaurantName(request.restaurantName().trim())
                        .menuItemId(request.menuItemId())
                        .itemName(request.itemName().trim())
                        .unitPrice(request.unitPrice())
                        .quantity(request.quantity())
                        .imageUrl(request.imageUrl())
                        .category(request.category())
                        .build());
        return toResponse(cartItemRepository.save(item));
    }

    public CartItemResponse updateQuantity(Long customerId, Long itemId, int delta) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow();
        if (!item.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Cart item not owned by customer.");
        }
        item.setQuantity(item.getQuantity() + delta);
        if (item.getQuantity() <= 0) {
            cartItemRepository.delete(item);
            return toResponse(item);
        }
        return toResponse(cartItemRepository.save(item));
    }

    public void remove(Long customerId, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow();
        if (!item.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Cart item not owned by customer.");
        }
        cartItemRepository.delete(item);
    }

    public void clear(Long customerId) {
        cartItemRepository.deleteByCustomerId(customerId);
    }

    private CartItemResponse toResponse(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getCustomerId(),
                item.getRestaurantId(),
                item.getRestaurantName(),
                item.getMenuItemId(),
                item.getItemName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getImageUrl(),
                item.getCategory(),
                item.getCreatedAt()
        );
    }
}
