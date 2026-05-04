package com.quickbite.cartservice.dto;

import java.util.List;

public record CartSummaryResponse(List<CartItemResponse> items, double subtotal) {
}
