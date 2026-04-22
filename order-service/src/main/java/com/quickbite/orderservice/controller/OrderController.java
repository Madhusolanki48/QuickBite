package com.quickbite.orderservice.controller;

import com.quickbite.orderservice.dto.*;
import com.quickbite.orderservice.model.*;
import com.quickbite.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> all() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> one(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping("/internal/{id}")
    public ResponseEntity<OrderResponse> internalOne(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.create(request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> status(@PathVariable Long id, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<OrderResponse> payment(@PathVariable Long id, @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(orderService.updatePaymentStatus(id, status));
    }
}
