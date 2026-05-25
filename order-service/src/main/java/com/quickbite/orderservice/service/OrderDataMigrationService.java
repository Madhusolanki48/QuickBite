package com.quickbite.orderservice.service;

import com.quickbite.orderservice.model.DeliveryStatus;
import com.quickbite.orderservice.model.FoodOrder;
import com.quickbite.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDataMigrationService {

    private final OrderRepository orderRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateOrders() {
        log.info("Checking for orders requiring fulfillment field backfills...");
        List<FoodOrder> orders = orderRepository.findAll();
        int migratedCount = 0;
        for (FoodOrder order : orders) {
            boolean modified = false;
            if (order.getDeliveryAgentStatus() == null) {
                order.setDeliveryAgentStatus(DeliveryStatus.UNASSIGNED);
                modified = true;
            }
            if (order.getDeliveryAddress() == null || order.getDeliveryAddress().isBlank()) {
                order.setDeliveryAddress("Address on file");
                modified = true;
            }
            if (modified) {
                orderRepository.save(order);
                migratedCount++;
            }
        }
        log.info("Order data migration completed. Backfilled {} existing orders safely and idempotently.", migratedCount);
    }
}
