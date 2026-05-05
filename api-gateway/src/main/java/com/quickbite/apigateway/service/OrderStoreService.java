package com.quickbite.apigateway.service;

import com.quickbite.apigateway.dto.OrderRequest;
import com.quickbite.apigateway.dto.OrderResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderStoreService {
    private final AtomicLong sequence = new AtomicLong(1L);
    private final ConcurrentHashMap<Long, OrderResponse> orders = new ConcurrentHashMap<>();

    public OrderResponse create(OrderRequest request) {
        long id = sequence.getAndIncrement();
        OrderResponse order = new OrderResponse(
                id,
                request.customerId(),
                request.restaurantId(),
                request.customerEmail(),
                request.customerName(),
                request.totalAmount(),
                normalizeCurrency(request.currency()),
                request.paymentMethod(),
                normalizePaymentStatus(request.paymentStatus()),
                request.razorpayPaymentId(),
                request.razorpayOrderId(),
                request.razorpaySignature(),
                request.items() == null ? List.of() : List.copyOf(request.items()),
                Instant.now());
        orders.put(id, order);
        return order;
    }

    public List<OrderResponse> findAll() {
        return orders.values().stream()
                .sorted(Comparator.comparing(OrderResponse::createdAt).reversed())
                .toList();
    }

    private String normalizeCurrency(String currency) {
        return currency == null || currency.isBlank() ? "INR" : currency.trim().toUpperCase();
    }

    private String normalizePaymentStatus(String paymentStatus) {
        return paymentStatus == null || paymentStatus.isBlank() ? "SUCCESS" : paymentStatus.trim().toUpperCase();
    }
}
