package com.quickbite.paymentservice.dto;

import com.quickbite.paymentservice.model.PaymentMethod;
import jakarta.validation.constraints.*;

public record CreatePaymentRequest(@NotNull Long orderId, @NotNull Long customerId, @Positive double amount,
        @NotNull PaymentMethod paymentMethod) {
}
