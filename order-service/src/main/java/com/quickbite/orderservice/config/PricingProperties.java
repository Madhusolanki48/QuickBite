package com.quickbite.orderservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "quickbite.pricing")
public record PricingProperties(double deliveryFee, double gstRate) {
    public PricingProperties {
        if (deliveryFee < 0) deliveryFee = 49.0;
        if (gstRate < 0) gstRate = 0.05;
    }
}
