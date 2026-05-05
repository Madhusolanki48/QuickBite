package com.quickbite.paymentservice.service;

import com.quickbite.paymentservice.dto.CreatePaymentRequest;
import com.quickbite.paymentservice.model.Payment;
import com.quickbite.paymentservice.model.PaymentMethod;
import com.quickbite.paymentservice.model.PaymentStatus;
import com.quickbite.paymentservice.repository.PaymentRepository;
import com.quickbite.paymentservice.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private WalletTransactionRepository walletTransactionRepository;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, walletTransactionRepository);
    }

    @Test
    void createMarksPaymentSuccessful() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(9L);
            return payment;
        });

        var response = paymentService.create(new CreatePaymentRequest(12L, 1L, 707.0, PaymentMethod.UPI));

        assertThat(response.id()).isEqualTo(9L);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.UPI);
    }
}
