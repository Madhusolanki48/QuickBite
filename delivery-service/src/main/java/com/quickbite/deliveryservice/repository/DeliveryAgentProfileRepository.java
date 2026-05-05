package com.quickbite.deliveryservice.repository;

import com.quickbite.deliveryservice.model.DeliveryAgentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryAgentProfileRepository extends JpaRepository<DeliveryAgentProfile, Long> {
    Optional<DeliveryAgentProfile> findByUserId(Long userId);
}
