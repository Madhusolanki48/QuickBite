package com.quickbite.deliveryservice.service;

import com.quickbite.deliveryservice.dto.CreateDeliveryRequest;
import com.quickbite.deliveryservice.model.DeliveryAssignment;
import com.quickbite.deliveryservice.model.DeliveryStatus;
import com.quickbite.deliveryservice.repository.DeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {
    @Mock
    private DeliveryRepository deliveryRepository;
    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        WebClient.Builder webClientBuilder = WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK).build()));
        deliveryService = new DeliveryService(deliveryRepository, webClientBuilder);
    }

    @Test
    void createAssignsDeliveryByDefault() {
        when(deliveryRepository.save(any(DeliveryAssignment.class))).thenAnswer(invocation -> {
            DeliveryAssignment delivery = invocation.getArgument(0);
            delivery.setId(3L);
            return delivery;
        });

        var response = deliveryService
                .create(new CreateDeliveryRequest(12L, 1L, "QuickBite Rider", "9999999999", "Navrangpura"));

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.deliveryStatus()).isEqualTo(DeliveryStatus.ASSIGNED);
    }
}
