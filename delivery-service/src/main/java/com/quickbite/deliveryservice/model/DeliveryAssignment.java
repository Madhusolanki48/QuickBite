package com.quickbite.deliveryservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long orderId;
    @Column(nullable = false)
    private Long riderId;
    @Column(nullable = false, length = 120)
    private String riderName;
    @Column(nullable = false, length = 20)
    private String riderPhone;
    @Column(nullable = false, length = 255)
    private String deliveryAddress;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus deliveryStatus;
    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null)
            createdAt = Instant.now();
        if (deliveryStatus == null)
            deliveryStatus = DeliveryStatus.ASSIGNED;
    }
}
