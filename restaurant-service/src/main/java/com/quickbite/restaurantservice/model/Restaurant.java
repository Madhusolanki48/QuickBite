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

    @Column(length = 500)
    private String description;
    private Integer minOrder;
    @Column(length = 20)
    private String gstin;
    @Column(length = 20)
    private String fssai;

    /** All menu items belonging to this restaurant (flat list, preserved from original design). */
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<MenuItem> menuItems = new ArrayList<>();

    /**
     * Menu categories owned by this restaurant.
     * Each category is scoped to this restaurant — owners manage their own category hierarchy.
     */
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<MenuCategory> menuCategories = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OperatingHour> operatingHours = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null)
            createdAt = Instant.now();
        if (status == null)
            status = RestaurantStatus.ACTIVE;
    }
}
