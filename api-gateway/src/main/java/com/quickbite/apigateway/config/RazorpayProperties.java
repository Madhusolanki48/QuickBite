package com.quickbite.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "razorpay")
public record RazorpayProperties(
        String keyId,
        String keySecret,
        String currency,
        String apiBaseUrl) {
}
