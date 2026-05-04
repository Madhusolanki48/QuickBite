package com.quickbite.paymentservice.dto;

import com.quickbite.paymentservice.model.WalletTransactionType;

import java.time.Instant;

public record WalletTransactionResponse(
                Long id,
                Long customerId,
                String title,
                double amount,
                WalletTransactionType type,
                Instant createdAt) {
}
