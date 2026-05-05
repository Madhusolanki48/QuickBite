package com.quickbite.apigateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.quickbite.apigateway.config.RazorpayProperties;
import com.quickbite.apigateway.dto.CreateOrderRequest;
import com.quickbite.apigateway.dto.CreateOrderResponse;
import com.quickbite.apigateway.dto.VerifyPaymentRequest;
import com.quickbite.apigateway.dto.VerifyPaymentResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RazorpayClientService {
    private final RazorpayProperties properties;
    private final WebClient webClient;

    public RazorpayClientService(RazorpayProperties properties, WebClient.Builder webClientBuilder) {
        this.properties = properties;
        this.webClient = webClientBuilder.build();
    }

    public Mono<CreateOrderResponse> createOrder(CreateOrderRequest request) {
        validateConfiguration();
        long amountInPaise = request.amount().longValueExact();
        BigDecimal amount = BigDecimal.valueOf(amountInPaise);
        String currency = normalizeCurrency(request.currency());
        String receipt = request.receipt() != null && !request.receipt().isBlank()
                ? request.receipt()
                : buildReceipt(request);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("amount", amountInPaise);
        payload.put("currency", currency);
        payload.put("receipt", receipt);
        payload.put("payment_capture", 1);
        if (request.notes() != null && !request.notes().isEmpty()) {
            payload.put("notes", request.notes());
        }

        return webClient.post()
                .uri(properties.apiBaseUrl() + "/orders")
                .headers(headers -> headers.setBasicAuth(properties.keyId(), properties.keySecret()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> {
                    if (response == null || response.path("id").isMissingNode()) {
                        throw new IllegalStateException("Razorpay order creation returned an empty response");
                    }

                    String razorpayOrderId = response.path("id").asText();
                    return new CreateOrderResponse(
                            razorpayOrderId,
                            razorpayOrderId,
                            amount,
                            currency,
                            properties.keyId(),
                            receipt);
                });
    }

    public VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request) {
        String expectedSignature = hmacSha256(
                request.razorpayOrderId() + "|" + request.razorpayPaymentId(),
                properties.keySecret());
        boolean verified = MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                request.razorpaySignature().getBytes(StandardCharsets.UTF_8));

        return new VerifyPaymentResponse(
                verified,
                verified ? "Payment verified successfully" : "Invalid Razorpay signature",
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature());
    }

    private String normalizeCurrency(String currency) {
        return currency == null || currency.isBlank() ? properties.currency() : currency.trim().toUpperCase();
    }

    private void validateConfiguration() {
        if (properties.keyId() == null || properties.keyId().isBlank() || properties.keySecret() == null || properties.keySecret().isBlank()) {
            throw new IllegalStateException("Razorpay key_id/key_secret are not configured");
        }
    }

    private String buildReceipt(CreateOrderRequest request) {
        String prefix = request.customerId() != null ? "customer-" + request.customerId() : "quickbite";
        return prefix + "-" + System.currentTimeMillis();
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to verify Razorpay signature", ex);
        }
    }
}
