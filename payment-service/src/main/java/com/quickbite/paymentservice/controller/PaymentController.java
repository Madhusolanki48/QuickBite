package com.quickbite.paymentservice.controller;

import com.quickbite.paymentservice.dto.*;
import com.quickbite.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> all() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> one(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(paymentService.create(request));
    }

    @PatchMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refund(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.refund(id));
    }

    @GetMapping("/wallet/{customerId}")
    public ResponseEntity<?> walletTransactions(@PathVariable Long customerId) {
        return ResponseEntity.ok(paymentService.walletTransactions(customerId));
    }

    @GetMapping("/wallet/{customerId}/balance")
    public ResponseEntity<Double> walletBalance(@PathVariable Long customerId) {
        return ResponseEntity.ok(paymentService.walletBalance(customerId));
    }

    @PostMapping("/wallet/{customerId}/top-up")
    public ResponseEntity<WalletTransactionResponse> topUp(@PathVariable Long customerId,
            @Valid @RequestBody WalletTopUpRequest request) {
        return ResponseEntity.ok(paymentService.topUp(customerId, request));
    }

    @PostMapping("/wallet/{customerId}/spend")
    public ResponseEntity<WalletTransactionResponse> spend(@PathVariable Long customerId,
            @Valid @RequestBody WalletSpendRequest request) {
        return ResponseEntity.ok(paymentService.spend(customerId, request));
    }
}
