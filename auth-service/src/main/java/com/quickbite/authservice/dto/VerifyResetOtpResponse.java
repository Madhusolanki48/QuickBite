package com.quickbite.authservice.dto;

public record VerifyResetOtpResponse(String message, boolean verified, String resetToken, long expiresInMs) {
}
