package com.quickbite.apigateway.controller;

import com.quickbite.apigateway.dto.CreateOrderRequest;
import com.quickbite.apigateway.dto.CreateOrderResponse;
import com.quickbite.apigateway.dto.VerifyPaymentRequest;
import com.quickbite.apigateway.dto.VerifyPaymentResponse;
import com.quickbite.apigateway.service.RazorpayClientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class PaymentController {
    private final RazorpayClientService razorpayClientService;

    public PaymentController(RazorpayClientService razorpayClientService) {
        this.razorpayClientService = razorpayClientService;
    }

    @PostMapping({
            "/api/payment/create-order",
            "/api/payments/create-order",
            "/payment/create-order",
            "/payments/create-order",
            "/create-order"
    })
    public Mono<ResponseEntity<CreateOrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return razorpayClientService.createOrder(request).map(ResponseEntity::ok);
    }

    @PostMapping({
            "/api/payment/verify",
            "/api/payments/verify",
            "/payment/verify",
            "/payments/verify",
            "/verify"
    })
    public ResponseEntity<VerifyPaymentResponse> verify(@Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(razorpayClientService.verifyPayment(request));
    }
}
