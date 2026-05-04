package com.quickbite.paymentservice.service;

import com.quickbite.paymentservice.dto.CreatePaymentRequest;
import com.quickbite.paymentservice.dto.PaymentResponse;
import com.quickbite.paymentservice.dto.WalletSpendRequest;
import com.quickbite.paymentservice.dto.WalletTopUpRequest;
import com.quickbite.paymentservice.dto.WalletTransactionResponse;
import com.quickbite.paymentservice.exception.NotFoundException;
import com.quickbite.paymentservice.model.Payment;
import com.quickbite.paymentservice.model.PaymentMethod;
import com.quickbite.paymentservice.model.PaymentStatus;
import com.quickbite.paymentservice.model.WalletTransaction;
import com.quickbite.paymentservice.model.WalletTransactionType;
import com.quickbite.paymentservice.repository.PaymentRepository;
import com.quickbite.paymentservice.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public List<PaymentResponse> findAll() {
        return paymentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PaymentResponse findById(Long id) {
        return toResponse(paymentRepository.findById(id).orElseThrow(() -> new NotFoundException("Payment not found")));
    }

    public PaymentResponse create(CreatePaymentRequest request) {
        PaymentStatus status = request.paymentMethod() == PaymentMethod.CASH_ON_DELIVERY ? PaymentStatus.PENDING : PaymentStatus.SUCCESS;
        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .customerId(request.customerId())
                .amount(request.amount())
                .paymentMethod(request.paymentMethod())
                .paymentStatus(status)
                .build();
        Payment saved = paymentRepository.save(payment);

        if (status == PaymentStatus.SUCCESS && request.paymentMethod() == PaymentMethod.WALLET) {
            spend(request.customerId(), new WalletSpendRequest(request.amount(), "Payment for order #" + request.orderId()));
        }

        return toResponse(saved);
    }

    public PaymentResponse refund(Long id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new NotFoundException("Payment not found"));
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        Payment saved = paymentRepository.save(payment);
        topUp(saved.getCustomerId(), new WalletTopUpRequest(saved.getAmount(), "Refund for order #" + saved.getOrderId()));
        return toResponse(saved);
    }

    public List<WalletTransactionResponse> walletTransactions(Long customerId) {
        return walletTransactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public double walletBalance(Long customerId) {
        return walletTransactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .mapToDouble(txn -> txn.getType() == WalletTransactionType.CREDIT ? txn.getAmount() : -txn.getAmount())
                .sum();
    }

    public WalletTransactionResponse topUp(Long customerId, WalletTopUpRequest request) {
        WalletTransaction transaction = WalletTransaction.builder()
                .customerId(customerId)
                .title(request.title() == null || request.title().isBlank() ? "Wallet top-up" : request.title().trim())
                .amount(request.amount())
                .type(WalletTransactionType.CREDIT)
                .build();
        return toResponse(walletTransactionRepository.save(transaction));
    }

    public WalletTransactionResponse spend(Long customerId, WalletSpendRequest request) {
        double balance = walletBalance(customerId);
        if (balance < request.amount()) {
            throw new IllegalArgumentException("Insufficient wallet balance.");
        }

        WalletTransaction transaction = WalletTransaction.builder()
                .customerId(customerId)
                .title(request.title() == null || request.title().isBlank() ? "Wallet payment" : request.title().trim())
                .amount(request.amount())
                .type(WalletTransactionType.DEBIT)
                .build();
        return toResponse(walletTransactionRepository.save(transaction));
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrderId(), p.getCustomerId(), p.getAmount(), p.getPaymentMethod(), p.getPaymentStatus(), p.getCreatedAt());
    }

    private WalletTransactionResponse toResponse(WalletTransaction txn) {
        return new WalletTransactionResponse(txn.getId(), txn.getCustomerId(), txn.getTitle(), txn.getAmount(), txn.getType(), txn.getCreatedAt());
    }
}
