package com.quickbite.deliveryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DeliveryAgentProfileRequest(
                @NotNull Long userId,
                @NotBlank @Size(min = 3, max = 120) String fullName,
                @NotBlank String email,
                @NotBlank @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits") String phoneNumber,
                @NotBlank String vehicleType,
                @NotBlank String vehicleNumber,
                String vehicleModel,
                String licenseNumber,
                String serviceArea,
                Boolean active) {
}
