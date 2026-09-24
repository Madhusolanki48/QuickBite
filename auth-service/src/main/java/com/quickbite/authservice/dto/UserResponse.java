package com.quickbite.authservice.dto;

import com.quickbite.authservice.model.Role;
import com.quickbite.authservice.model.ApprovalStatus;
import com.quickbite.authservice.model.OnboardingStatus;
import java.time.Instant;

public record UserResponse(
                Long id,
                String firstName,
                String lastName,
                String email,
                String phoneNumber,
                Role role,
                String restaurantId,
                String restaurantName,
                ApprovalStatus approvalStatus,
                OnboardingStatus onboardingStatus,
                String rejectionReason,
                Instant createdAt) {
}
