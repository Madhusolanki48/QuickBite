package com.quickbite.deliveryservice.controller;

import com.quickbite.deliveryservice.dto.DeliveryAgentProfileRequest;
import com.quickbite.deliveryservice.dto.DeliveryAgentProfileResponse;
import com.quickbite.deliveryservice.service.DeliveryAgentProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-agents")
@RequiredArgsConstructor
public class DeliveryAgentProfileController {
    private final DeliveryAgentProfileService service;

    @GetMapping
    public ResponseEntity<List<DeliveryAgentProfileResponse>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<DeliveryAgentProfileResponse> one(@PathVariable Long userId) {
        return ResponseEntity.ok(service.findByUserId(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<DeliveryAgentProfileResponse> upsert(@PathVariable Long userId,
            @Valid @RequestBody DeliveryAgentProfileRequest request) {
        return ResponseEntity.ok(service.save(new DeliveryAgentProfileRequest(
                userId,
                request.fullName(),
                request.email(),
                request.phoneNumber(),
                request.vehicleType(),
                request.vehicleNumber(),
                request.vehicleModel(),
                request.licenseNumber(),
                request.serviceArea(),
                request.active())));
    }
}
