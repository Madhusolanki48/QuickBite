package com.quickbite.orderservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long customerId;
    @Column(nullable = false)
    private Long restaurantId;
    @Column(nullable = false, length = 120)
    private String customerEmail;
    @Column(nullable = false)
    private double totalAmount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;
    @Column(nullable = false)
    private Instant createdAt;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FoodOrderItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null)
            createdAt = Instant.now();
        if (orderStatus == null)
            orderStatus = OrderStatus.CREATED;
        if (paymentStatus == null)
            paymentStatus = PaymentStatus.PENDING;
    }
}
