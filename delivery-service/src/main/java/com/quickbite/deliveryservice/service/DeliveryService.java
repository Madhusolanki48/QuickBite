package com.quickbite.deliveryservice.service;

import com.quickbite.deliveryservice.dto.CreateDeliveryRequest;
import com.quickbite.deliveryservice.dto.DeliveryResponse;
import com.quickbite.deliveryservice.exception.NotFoundException;
import com.quickbite.deliveryservice.model.DeliveryAssignment;
import com.quickbite.deliveryservice.model.DeliveryStatus;
import com.quickbite.deliveryservice.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${ORDER_SERVICE_URL:http://order-service:8083}")
    private String orderServiceUrl;

    @Value("${quickbite.internal.service-key:quickbite-internal-secret-token}")
    private String internalServiceKey;

    public List<DeliveryResponse> findAll() {
        return deliveryRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DeliveryResponse findById(Long id) {
        return toResponse(deliveryRepository.findById(id).orElseThrow(() -> new NotFoundException("Delivery not found")));
    }

    public DeliveryResponse create(CreateDeliveryRequest request) {
        verifyOrderExists(request.orderId());

        DeliveryAssignment assignment = deliveryRepository.findByOrderId(request.orderId())
                .orElseGet(() -> DeliveryAssignment.builder().orderId(request.orderId()).build());

        assignment.setRiderId(request.riderId());
        assignment.setRiderName(request.riderName());
        assignment.setRiderPhone(request.riderPhone());
        assignment.setDeliveryAddress(request.deliveryAddress());
        assignment.setDeliveryStatus(DeliveryStatus.ASSIGNED);

        return toResponse(deliveryRepository.save(assignment));
    }

    public DeliveryResponse updateStatus(Long id, DeliveryStatus status) {
        DeliveryAssignment assignment = deliveryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Delivery not found"));
        assignment.setDeliveryStatus(status);
        DeliveryAssignment saved = deliveryRepository.save(assignment);

        // Propagate status to order-service (authoritative source of truth)
        syncStatusToOrderService(saved.getOrderId(), status);

        return toResponse(saved);
    }

    private void syncStatusToOrderService(Long orderId, DeliveryStatus status) {
        if (orderId == null) {
            return;
        }
        try {
            String targetOrderStatus = null;
            String targetDeliveryStatus = status.name();

            if (status == DeliveryStatus.PICKED_UP) {
                targetOrderStatus = "OUT_FOR_DELIVERY";
            } else if (status == DeliveryStatus.DELIVERED) {
                targetOrderStatus = "DELIVERED";
            } else if (status == DeliveryStatus.CANCELLED) {
                targetOrderStatus = "CANCELLED";
            }

            String url = orderServiceUrl.trim().replaceAll("/+$", "") + "/api/orders/internal/" + orderId + "/status";
            StringBuilder uriBuilder = new StringBuilder(url).append("?deliveryStatus=").append(targetDeliveryStatus);
            if (targetOrderStatus != null) {
                uriBuilder.append("&orderStatus=").append(targetOrderStatus);
            }

            webClientBuilder.build()
                    .patch()
                    .uri(uriBuilder.toString())
                    .header("X-Internal-Service-Key", internalServiceKey)
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(4));

            log.info("Successfully synced delivery status {} to order-service for order {}", status, orderId);
        } catch (Exception ex) {
            log.warn("Could not sync delivery status to order-service for order {}: {}", orderId, ex.getMessage());
        }
    }

    private void verifyOrderExists(Long orderId) {
        if (orderId == null) return;
        try {
            webClientBuilder.build()
                    .get()
                    .uri(orderServiceUrl.trim().replaceAll("/+$", "") + "/api/orders/internal/{id}", orderId)
                    .header("X-Internal-Service-Key", internalServiceKey)
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(4));
        } catch (WebClientResponseException.NotFound ex) {
            log.warn("Order {} not found in order-service", orderId);
            // Allow assignment creation anyway to avoid blocking flow
        } catch (Exception ex) {
            log.warn("Order lookup failed in order-service: {}", ex.getMessage());
        }
    }

    private DeliveryResponse toResponse(DeliveryAssignment d) {
        return new DeliveryResponse(d.getId(), d.getOrderId(), d.getRiderId(), d.getRiderName(), d.getRiderPhone(), d.getDeliveryAddress(), d.getDeliveryStatus(), d.getCreatedAt());
    }
}
