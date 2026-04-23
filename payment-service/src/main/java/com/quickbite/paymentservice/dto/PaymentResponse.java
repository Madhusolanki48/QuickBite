package com.quickbite.paymentservice.dto;

import com.quickbite.paymentservice.model.*;
import java.time.Instant;

public record PaymentResponse(Long id, Long orderId, Long customerId, double amount, PaymentMethod paymentMethod,
        PaymentStatus paymentStatus, Instant createdAt) {
}
