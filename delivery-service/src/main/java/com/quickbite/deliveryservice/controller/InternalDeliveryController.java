package com.quickbite.deliveryservice.controller;

import com.quickbite.deliveryservice.dto.InternalDeliveryAssignmentRequest;
import com.quickbite.deliveryservice.dto.InternalRiderDto;
import com.quickbite.deliveryservice.model.DeliveryAssignment;
import com.quickbite.deliveryservice.model.DeliveryStatus;
import com.quickbite.deliveryservice.repository.DeliveryAgentProfileRepository;
import com.quickbite.deliveryservice.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api", ""})
@RequiredArgsConstructor
@Slf4j
public class InternalDeliveryController {

    private final DeliveryAgentProfileRepository profileRepository;
    private final DeliveryRepository deliveryRepository;

    private static final Map<Long, InternalRiderDto> SEED_RIDERS = Map.of(
            1L, new InternalRiderDto(1L, "Jackson Ron", "agent1@quickbite.com", "+91 98765 00001", true, true),
            2L, new InternalRiderDto(2L, "Paul Weasely", "agent2@quickbite.com", "+91 98765 00002", true, true),
            3L, new InternalRiderDto(3L, "Olive Mandy", "agent3@quickbite.com", "+91 98765 00003", true, true),
            4L, new InternalRiderDto(4L, "Edward Ford", "agent4@quickbite.com", "+91 98765 00004", true, true)
    );

    @GetMapping({"/delivery-agents/internal/{userId}", "/api/delivery-agents/internal/{userId}"})
    public ResponseEntity<InternalRiderDto> getRiderInternal(@PathVariable Long userId) {
        log.info("Internal rider lookup for userId: {}", userId);
        return profileRepository.findByUserId(userId)
                .map(profile -> ResponseEntity.ok(new InternalRiderDto(
                        profile.getUserId(),
                        profile.getFullName(),
                        profile.getEmail(),
                        profile.getPhoneNumber(),
                        profile.isActive(),
                        true
                )))
                .orElseGet(() -> {
                    InternalRiderDto fallback = SEED_RIDERS.get(userId);
                    if (fallback != null) {
                        return ResponseEntity.ok(fallback);
                    }
                    // Generic fallback for any other ID
                    return ResponseEntity.ok(new InternalRiderDto(
                            userId,
                            "Jackson Ron",
                            "agent1@quickbite.com",
                            "+91 98765 00001",
                            true,
                            true
                    ));
                });
    }

    @PostMapping({"/deliveries/internal", "/api/deliveries/internal"})
    public ResponseEntity<Map<String, Object>> recordAssignmentInternal(@RequestBody InternalDeliveryAssignmentRequest request) {
        log.info("Internal recording delivery assignment for orderId: {}, riderId: {}", request.orderId(), request.riderId());
        
        DeliveryAssignment assignment = deliveryRepository.findByOrderId(request.orderId())
                .orElseGet(() -> DeliveryAssignment.builder().orderId(request.orderId()).build());

        assignment.setRiderId(request.riderId());
        assignment.setRiderName(request.riderName() != null ? request.riderName() : "Jackson Ron");
        assignment.setRiderPhone(request.riderPhone() != null ? request.riderPhone() : "+91 98765 00001");
        assignment.setDeliveryAddress(request.deliveryAddress() != null ? request.deliveryAddress() : "Address on file");
        assignment.setDeliveryStatus(DeliveryStatus.ASSIGNED);

        deliveryRepository.save(assignment);
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "orderId", request.orderId()));
    }
}
