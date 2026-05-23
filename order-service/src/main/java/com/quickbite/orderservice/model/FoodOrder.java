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

    @Column(length = 120)
    private String customerName;

    @Column(length = 20)
    private String customerPhone;

    @Column(length = 500)
    private String deliveryAddress;

    @Column(length = 500)
    private String note;

    private Long deliveryAgentId;

    @Column(length = 120)
    private String deliveryAgentName;

    @Column(length = 120)
    private String deliveryAgentEmail;

    @Column(length = 20)
    private String deliveryAgentPhone;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private DeliveryStatus deliveryAgentStatus = DeliveryStatus.UNASSIGNED;

    @Column(nullable = false)
    private double totalAmount;

    @Column(nullable = false)
    @Builder.Default
    private double deliveryFee = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private double gst = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private double discountAmount = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private double subtotal = 0.0;

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
        if (deliveryAgentStatus == null)
            deliveryAgentStatus = DeliveryStatus.UNASSIGNED;
    }
}
