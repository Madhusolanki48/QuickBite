package com.quickbite.authservice.dto;

public record ValidationResponse(boolean valid, String email, String role, String restaurantId, String approvalStatus) {
}
