package com.quickbite.authservice.dto;

public record AuthResponse(String token, String tokenType, long expiresInMs, UserResponse user) {
}
