package com.quickbite.restaurantservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI quickbiteOpenAPI() {
        return new OpenAPI().info(
                new Info()
                        .title("QuickBite Restaurant Service API")
                        .version("v1")
                        .description("APIs for restaurant, menu, and owner operations in QuickBite"));
    }
}
