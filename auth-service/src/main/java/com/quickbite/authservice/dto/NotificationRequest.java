package com.quickbite.authservice.dto;

import com.quickbite.authservice.model.Role;
import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
                String recipientEmail,
                Role recipientRole,
                @NotBlank String title,
                @NotBlank String message,
                @NotBlank String category) {
}
