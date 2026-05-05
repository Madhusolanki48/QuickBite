package com.quickbite.apigateway.controller;

import com.quickbite.apigateway.dto.OrderRequest;
import com.quickbite.apigateway.dto.OrderResponse;
import com.quickbite.apigateway.service.OrderStoreService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderStoreService orderStoreService;

    public OrderController(OrderStoreService orderStoreService) {
        this.orderStoreService = orderStoreService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderStoreService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> list() {
        return ResponseEntity.ok(orderStoreService.findAll());
    }
}
