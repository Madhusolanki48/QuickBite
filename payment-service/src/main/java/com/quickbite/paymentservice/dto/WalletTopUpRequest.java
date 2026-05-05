package com.quickbite.paymentservice.dto;

import jakarta.validation.constraints.Positive;

public record WalletTopUpRequest(@Positive double amount, String title) {
}
