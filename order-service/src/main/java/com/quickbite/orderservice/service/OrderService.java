package com.quickbite.orderservice.service;

import com.quickbite.orderservice.dto.*;
import com.quickbite.orderservice.exception.NotFoundException;
import com.quickbite.orderservice.model.*;
import com.quickbite.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service @RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MailService mailService;
    public List<OrderResponse> findAll(){ return orderRepository.findAll().stream().map(this::toResponse).toList(); }
    public OrderResponse findById(Long id){ return toResponse(orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"))); }
    public OrderResponse create(CreateOrderRequest request){
        FoodOrder order = FoodOrder.builder()
                .customerId(request.customerId())
                .restaurantId(request.restaurantId())
                .customerEmail(request.customerEmail() == null ? null : request.customerEmail().trim().toLowerCase())
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        request.items().forEach(i -> order.getItems().add(FoodOrderItem.builder().menuItemId(i.menuItemId()).itemName(i.itemName()).quantity(i.quantity()).unitPrice(i.unitPrice()).build()));
        double subtotal = order.getItems().stream().mapToDouble(i -> i.getUnitPrice() * i.getQuantity()).sum();
        double discount = request.discountAmount() == null ? 0.0 : Math.max(0.0, request.discountAmount());
        order.setTotalAmount(Math.max(0.0, subtotal - discount));
        FoodOrder saved = orderRepository.save(order);
        try {
            mailService.sendOrderConfirmation(saved);
        } catch (RuntimeException ex) {
            // Order creation should still succeed if email delivery fails.
        }
        return toResponse(saved);
    }
    public OrderResponse updateStatus(Long id, OrderStatus status){ FoodOrder order=orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found")); order.setOrderStatus(status); return toResponse(orderRepository.save(order)); }
    public OrderResponse updatePaymentStatus(Long id, PaymentStatus status){ FoodOrder order=orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found")); order.setPaymentStatus(status); return toResponse(orderRepository.save(order)); }
    private OrderResponse toResponse(FoodOrder o){ return new OrderResponse(o.getId(),o.getCustomerId(),o.getRestaurantId(),o.getCustomerEmail(),o.getTotalAmount(),o.getOrderStatus(),o.getPaymentStatus(),o.getCreatedAt(),o.getItems().stream().map(i -> new OrderItemResponse(i.getId(),i.getMenuItemId(),i.getItemName(),i.getQuantity(),i.getUnitPrice())).toList()); }
}
