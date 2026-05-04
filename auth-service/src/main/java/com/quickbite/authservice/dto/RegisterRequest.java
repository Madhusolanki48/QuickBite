package com.quickbite.authservice.dto;

import com.quickbite.authservice.model.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(
                @NotBlank @Size(min = 2, max = 50) String firstName,
                @NotBlank @Size(min = 2, max = 50) String lastName,
                @NotBlank @Email String email,
                @NotBlank @Pattern(regexp = "^[0-9]{10,15}$") String phoneNumber,
                @NotBlank @Size(min = 8, max = 100) String password,
                @NotNull Role role,
                String restaurantId) {
}
