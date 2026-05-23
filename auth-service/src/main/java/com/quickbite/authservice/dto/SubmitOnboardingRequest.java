package com.quickbite.authservice.dto;

public record SubmitOnboardingRequest(
        String restaurantId,
        String restaurantName
) {}
