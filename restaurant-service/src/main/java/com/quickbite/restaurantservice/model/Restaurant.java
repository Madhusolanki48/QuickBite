package com.quickbite.restaurantservice.model;

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
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 255)
    private String address;
    @Column(nullable = false, length = 20)
    private String phoneNumber;
    @Column(nullable = false, length = 120)
    private String email;
    private Long ownerId;
    @Column(length = 120)
    private String ownerEmail;
    @Column(length = 120)
    private String ownerName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CuisineType cuisineType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantStatus status;
    @Column(nullable = false)
    private double rating;
    @Column(nullable = false)
    private Instant createdAt;
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<MenuItem> menuItems = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null)
            createdAt = Instant.now();
        if (status == null)
            status = RestaurantStatus.ACTIVE;
    }
}
