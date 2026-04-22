package com.quickbite.authservice.dto;

public record GoogleProfile(
                String subject,
                String email,
                String firstName,
                String lastName,
                String picture,
                boolean emailVerified) {
}
