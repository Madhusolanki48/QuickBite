package com.quickbite.paymentservice.dto;

import jakarta.validation.constraints.Positive;

public record WalletSpendRequest(@Positive double amount, String title) {
}
