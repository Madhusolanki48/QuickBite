package com.quickbite.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendRegistrationOtpRequest(@NotBlank @Email String email) {
}
