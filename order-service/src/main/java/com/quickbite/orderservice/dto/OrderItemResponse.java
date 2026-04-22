package com.quickbite.orderservice.dto;

public record OrderItemResponse(Long id, Long menuItemId, String itemName, int quantity, double unitPrice) {
}
