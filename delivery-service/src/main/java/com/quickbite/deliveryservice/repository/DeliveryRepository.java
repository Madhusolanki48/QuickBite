package com.quickbite.deliveryservice.repository;

import com.quickbite.deliveryservice.model.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<DeliveryAssignment, Long> {
    Optional<DeliveryAssignment> findByOrderId(Long orderId);
}
