package com.quickbite.authservice.dto;

import com.quickbite.authservice.model.Role;

import java.time.Instant;

public record NotificationResponse(
                Long id,
                String recipientEmail,
                Role recipientRole,
                String title,
                String message,
                String category,
                boolean read,
                Instant createdAt,
                Instant readAt) {
}
