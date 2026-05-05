package com.quickbite.apigateway.dto;

import java.time.Instant;

public record ApiErrorResponse(
        String error,
        String message,
        int status,
        Instant timestamp) {
}
