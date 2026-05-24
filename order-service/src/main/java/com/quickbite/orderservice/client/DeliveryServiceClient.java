package com.quickbite.orderservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;

@Component
@Slf4j
public class DeliveryServiceClient {

    private final WebClient webClient;
    private final String deliveryServiceUrl;
    private final String internalServiceKey;

    public record InternalRiderDto(
            Long userId,
            String fullName,
            String email,
            String phoneNumber,
            boolean active,
            boolean eligible
    ) {}

    public DeliveryServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.delivery-service.url:http://localhost:8085}") String deliveryServiceUrl,
            @Value("${quickbite.internal.service-key:quickbite-internal-secret-token}") String internalServiceKey) {
        this.webClient = webClientBuilder.build();
        this.deliveryServiceUrl = deliveryServiceUrl.trim().replaceAll("/+$", "");
        this.internalServiceKey = internalServiceKey;
    }

    public InternalRiderDto validateAndGetRider(Long riderId) {
        if (riderId == null) {
            throw new IllegalArgumentException("riderId is required");
        }
        try {
            InternalRiderDto dto = webClient.get()
                    .uri(deliveryServiceUrl + "/api/delivery-agents/internal/{userId}", riderId)
                    .header("X-Internal-Service-Key", internalServiceKey)
                    .retrieve()
                    .bodyToMono(InternalRiderDto.class)
                    .block(Duration.ofSeconds(5));

            if (dto == null) {
                throw new IllegalArgumentException("Delivery agent profile not found for riderId: " + riderId);
            }
            if (!dto.active()) {
                throw new IllegalArgumentException("Delivery agent '" + dto.fullName() + "' is currently marked as unavailable/inactive");
            }
            return dto;
        } catch (WebClientResponseException.NotFound ex) {
            throw new IllegalArgumentException("Delivery agent not found for riderId: " + riderId);
        } catch (WebClientResponseException ex) {
            throw new IllegalArgumentException("Delivery validation failed: " + ex.getResponseBodyAsString(), ex);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to connect to delivery-service: {}", ex.getMessage());
            throw new IllegalStateException("Delivery service communication failure: " + ex.getMessage(), ex);
        }
    }

    public void recordAssignment(Long orderId, Long riderId, String riderName, String riderPhone, String deliveryAddress) {
        try {
            Map<String, Object> body = Map.of(
                    "orderId", orderId,
                    "riderId", riderId,
                    "riderName", riderName != null ? riderName : "Assigned Rider",
                    "riderPhone", riderPhone != null ? riderPhone : "+91 98765 43210",
                    "deliveryAddress", deliveryAddress != null ? deliveryAddress : "Address on file"
            );
            webClient.post()
                    .uri(deliveryServiceUrl + "/api/deliveries/internal")
                    .header("X-Internal-Service-Key", internalServiceKey)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(5));
            log.info("Successfully notified delivery-service of assignment for orderId: {}", orderId);
        } catch (Exception ex) {
            log.warn("Could not record delivery assignment with delivery-service: {}", ex.getMessage());
        }
    }
}
