package com.quickbite.orderservice.controller;

import com.quickbite.orderservice.dto.*;
import com.quickbite.orderservice.model.*;
import com.quickbite.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/orders", "/orders"})
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    /**
     * GET /api/orders                    → all orders (admin)
     * GET /api/orders?restaurantId=1     → orders for specific restaurant (owner dashboard)
     * GET /api/orders?customerEmail=x@y  → orders for specific customer (my orders)
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> all(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) String customerEmail) {
        if (restaurantId != null && restaurantId > 0) {
            return ResponseEntity.ok(orderService.findByRestaurantId(restaurantId));
        }
        if (customerEmail != null && !customerEmail.isBlank()) {
            return ResponseEntity.ok(orderService.findByCustomerEmail(customerEmail.trim()));
        }
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> one(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.create(request));
    }

    @PutMapping("/{id}/assign-delivery")
    public ResponseEntity<OrderResponse> assignDelivery(
            @PathVariable Long id,
            @Valid @RequestBody AssignDeliveryRequest request,
            Authentication auth) {
        return ResponseEntity.ok(orderService.assignDelivery(id, request, auth));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> status(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            @RequestParam(required = false) String reason,
            Authentication auth) {
        return ResponseEntity.ok(orderService.updateStatus(id, status, auth, reason));
    }

    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<OrderResponse> payment(@PathVariable Long id, @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(orderService.updatePaymentStatus(id, status));
    }
}
