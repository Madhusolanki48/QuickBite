package com.quickbite.apigateway;

import com.quickbite.apigateway.config.RazorpayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RazorpayProperties.class)
public class GatewayApplicationConfig {
}
