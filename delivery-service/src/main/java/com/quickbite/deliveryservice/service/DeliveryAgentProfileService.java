package com.quickbite.deliveryservice.service;

import com.quickbite.deliveryservice.dto.DeliveryAgentProfileRequest;
import com.quickbite.deliveryservice.dto.DeliveryAgentProfileResponse;
import com.quickbite.deliveryservice.exception.NotFoundException;
import com.quickbite.deliveryservice.model.DeliveryAgentProfile;
import com.quickbite.deliveryservice.repository.DeliveryAgentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryAgentProfileService {
    private final DeliveryAgentProfileRepository repository;

    @Transactional(readOnly = true)
    public List<DeliveryAgentProfileResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DeliveryAgentProfileResponse findByUserId(Long userId) {
        return toResponse(repository.findByUserId(userId).orElseThrow(() -> new NotFoundException("Delivery agent profile not found")));
    }

    @Transactional
    public DeliveryAgentProfileResponse save(DeliveryAgentProfileRequest request) {
        DeliveryAgentProfile profile = repository.findByUserId(request.userId()).orElseGet(DeliveryAgentProfile::new);
        profile.setUserId(request.userId());
        profile.setFullName(request.fullName().trim());
        profile.setEmail(request.email().trim().toLowerCase());
        profile.setPhoneNumber(request.phoneNumber().trim());
        profile.setVehicleType(request.vehicleType().trim());
        profile.setVehicleNumber(request.vehicleNumber().trim().toUpperCase());
        profile.setVehicleModel(request.vehicleModel() != null ? request.vehicleModel().trim() : null);
        profile.setLicenseNumber(request.licenseNumber() != null ? request.licenseNumber().trim() : null);
        profile.setServiceArea(request.serviceArea() != null ? request.serviceArea().trim() : null);
        if (request.active() != null) {
            profile.setActive(request.active());
        }
        return toResponse(repository.save(profile));
    }

    private DeliveryAgentProfileResponse toResponse(DeliveryAgentProfile profile) {
        return new DeliveryAgentProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getFullName(),
                profile.getEmail(),
                profile.getPhoneNumber(),
                profile.getVehicleType(),
                profile.getVehicleNumber(),
                profile.getVehicleModel(),
                profile.getLicenseNumber(),
                profile.getServiceArea(),
                profile.isActive(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
