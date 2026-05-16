package com.quickbite.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyResetOtpRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 4, max = 10) String otp) {
}
