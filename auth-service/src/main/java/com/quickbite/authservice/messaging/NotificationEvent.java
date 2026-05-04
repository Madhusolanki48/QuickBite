package com.quickbite.authservice.messaging;

import com.quickbite.authservice.model.Role;

public record NotificationEvent(
                String recipientEmail,
                Role recipientRole,
                String title,
                String message,
                String category) {
}
