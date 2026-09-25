package com.quickbite.deliveryservice.dto;

public record InternalRiderDto(
        Long userId,
        String fullName,
        String email,
        String phoneNumber,
        boolean active,
        boolean eligible
) {}
