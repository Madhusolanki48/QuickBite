package com.quickbite.deliveryservice.repository;

import com.quickbite.deliveryservice.model.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<DeliveryAssignment, Long> {
}
