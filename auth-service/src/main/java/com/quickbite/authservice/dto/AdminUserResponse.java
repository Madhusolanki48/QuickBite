package com.quickbite.authservice.dto;

import com.quickbite.authservice.model.Role;
import com.quickbite.authservice.model.ApprovalStatus;

import java.time.Instant;

public record AdminUserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Role role,
        String restaurantId,
        String restaurantName,
        ApprovalStatus approvalStatus,
        boolean enabled,
        Instant createdAt) {
}
