package com.quickbite.deliveryservice.dto;

import java.time.Instant;

public record DeliveryAgentProfileResponse(
                Long id,
                Long userId,
                String fullName,
                String email,
                String phoneNumber,
                String vehicleType,
                String vehicleNumber,
                String vehicleModel,
                String licenseNumber,
                String serviceArea,
                boolean active,
                Instant createdAt,
                Instant updatedAt) {
}
