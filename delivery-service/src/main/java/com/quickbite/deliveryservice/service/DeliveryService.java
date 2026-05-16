package com.quickbite.deliveryservice.service;

import com.quickbite.deliveryservice.dto.CreateDeliveryRequest;
import com.quickbite.deliveryservice.dto.DeliveryResponse;
import com.quickbite.deliveryservice.exception.NotFoundException;
import com.quickbite.deliveryservice.model.DeliveryAssignment;
import com.quickbite.deliveryservice.model.DeliveryStatus;
import com.quickbite.deliveryservice.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final WebClient.Builder webClientBuilder;
    @Value("${ORDER_SERVICE_URL:http://order-service}")
    private String orderServiceUrl;

    public List<DeliveryResponse> findAll() {
        return deliveryRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DeliveryResponse findById(Long id) {
        return toResponse(deliveryRepository.findById(id).orElseThrow(() -> new NotFoundException("Delivery not found")));
    }

    public DeliveryResponse create(CreateDeliveryRequest request) {
        verifyOrderExists(request.orderId());

        DeliveryAssignment assignment = DeliveryAssignment.builder()
                .orderId(request.orderId())
                .riderId(request.riderId())
                .riderName(request.riderName())
                .riderPhone(request.riderPhone())
                .deliveryAddress(request.deliveryAddress())
                .deliveryStatus(DeliveryStatus.ASSIGNED)
                .build();
        return toResponse(deliveryRepository.save(assignment));
    }

    public DeliveryResponse updateStatus(Long id, DeliveryStatus status) {
        DeliveryAssignment assignment = deliveryRepository.findById(id).orElseThrow(() -> new NotFoundException("Delivery not found"));
        assignment.setDeliveryStatus(status);
        return toResponse(deliveryRepository.save(assignment));
    }

    private void verifyOrderExists(Long orderId) {
        try {
            webClientBuilder.build()
                    .get()
                    .uri(orderServiceUrl + "/api/orders/internal/{id}", orderId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new NotFoundException("Order not found");
        } catch (WebClientResponseException ex) {
            throw new NotFoundException("Order lookup failed");
        }
    }

    private DeliveryResponse toResponse(DeliveryAssignment d) {
        return new DeliveryResponse(d.getId(), d.getOrderId(), d.getRiderId(), d.getRiderName(), d.getRiderPhone(), d.getDeliveryAddress(), d.getDeliveryStatus(), d.getCreatedAt());
    }
}
