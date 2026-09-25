package com.quickbite.orderservice.controller;

import com.quickbite.orderservice.dto.OrderResponse;
import com.quickbite.orderservice.exception.NotFoundException;
import com.quickbite.orderservice.model.DeliveryStatus;
import com.quickbite.orderservice.model.FoodOrder;
import com.quickbite.orderservice.model.OrderStatus;
import com.quickbite.orderservice.repository.OrderRepository;
import com.quickbite.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/orders/internal", "/orders/internal"})
@RequiredArgsConstructor
@Slf4j
public class InternalOrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final com.quickbite.orderservice.messaging.OrderEventProducer eventProducer;

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderInternal(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatusInternal(
            @PathVariable Long id,
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(required = false) DeliveryStatus deliveryStatus) {
        FoodOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + id));

        log.info("Internal status update for order {}: orderStatus={}, deliveryStatus={}", id, orderStatus, deliveryStatus);
        if (orderStatus != null) {
            order.setOrderStatus(orderStatus);
        }
        if (deliveryStatus != null) {
            order.setDeliveryAgentStatus(deliveryStatus);
        }
        FoodOrder saved = orderRepository.save(order);
        OrderResponse response = orderService.findById(id);
        eventProducer.publishOrderStatusChanged(saved, response);
        return ResponseEntity.ok(response);
    }
}
