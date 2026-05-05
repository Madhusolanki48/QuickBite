package com.quickbite.deliveryservice.controller;

import com.quickbite.deliveryservice.dto.*;
import com.quickbite.deliveryservice.model.DeliveryStatus;
import com.quickbite.deliveryservice.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping
    public ResponseEntity<List<DeliveryResponse>> all() {
        return ResponseEntity.ok(deliveryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> one(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DeliveryResponse> create(
            @Valid @RequestBody CreateDeliveryRequest request) {
        return ResponseEntity.ok(deliveryService.create(request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> status(
            @PathVariable Long id,
            @RequestParam DeliveryStatus status) {
        return ResponseEntity.ok(deliveryService.updateStatus(id, status));
    }
}