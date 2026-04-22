package com.quickbite.authservice.dto;

public record UpdateProfileRequest(
                String firstName,
                String lastName,
                String phoneNumber,
                String password) {
}
