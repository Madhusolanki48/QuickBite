package com.quickbite.restaurantservice.dto;

public record OperatingHourResponse(
        Long id,
        String day,
        String openTime,
        String closeTime,
        boolean openToday
) {}
