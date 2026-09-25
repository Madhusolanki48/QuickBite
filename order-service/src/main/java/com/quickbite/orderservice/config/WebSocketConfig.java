package com.quickbite.orderservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register native WebSocket endpoint
        registry.addEndpoint("/api/orders/ws", "/orders/ws")
                .setAllowedOriginPatterns("*");

        // Register SockJS fallback endpoint
        registry.addEndpoint("/api/orders/ws", "/orders/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
