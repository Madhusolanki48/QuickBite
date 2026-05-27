package com.quickbite.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(PricingProperties.class)
public class WebClientConfig {

    @Value("${quickbite.internal.service-key:quickbite-internal-secret-token}")
    private String internalServiceKey;

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder()
                .defaultHeader("X-Internal-Service-Key", internalServiceKey);
    }
}
