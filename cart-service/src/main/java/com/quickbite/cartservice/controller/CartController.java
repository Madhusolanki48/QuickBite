package com.quickbite.cartservice.controller;

import com.quickbite.cartservice.dto.CartItemRequest;
import com.quickbite.cartservice.dto.CartItemResponse;
import com.quickbite.cartservice.dto.CartSummaryResponse;
import com.quickbite.cartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/{customerId}")
    public ResponseEntity<CartSummaryResponse> cart(@PathVariable Long customerId) {
        return ResponseEntity.ok(cartService.cart(customerId));
    }

    @PostMapping
    public ResponseEntity<CartItemResponse> add(@Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.add(request));
    }

    @PatchMapping("/{customerId}/items/{itemId}")
    public ResponseEntity<CartItemResponse> updateQuantity(@PathVariable Long customerId, @PathVariable Long itemId,
            @RequestParam int delta) {
        return ResponseEntity.ok(cartService.updateQuantity(customerId, itemId, delta));
    }

    @DeleteMapping("/{customerId}/items/{itemId}")
    public ResponseEntity<Void> remove(@PathVariable Long customerId, @PathVariable Long itemId) {
        cartService.remove(customerId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> clear(@PathVariable Long customerId) {
        cartService.clear(customerId);
        return ResponseEntity.noContent().build();
    }
}
