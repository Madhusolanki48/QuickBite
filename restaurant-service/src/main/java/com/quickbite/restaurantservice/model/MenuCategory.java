package com.quickbite.restaurantservice.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a menu category that belongs to exactly one Restaurant.
 * Example: "Burgers", "Beverages", "South Indian" — each scoped to the owning restaurant.
 */
@Entity
@Table(name = "menu_category",
        uniqueConstraints = @UniqueConstraint(name = "uq_category_restaurant_slug",
                columnNames = {"restaurant_id", "slug"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The restaurant that owns this category. Non-null, enforces Restaurant → MenuCategory. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    /** Human-readable category name, e.g. "Burgers", "South Indian". */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * URL-safe slug derived from name, unique per restaurant.
     * e.g. "south-indian", "quick-bites".
     */
    @Column(nullable = false, length = 100)
    private String slug;

    /** Optional emoji icon for the category, e.g. "🍔". */
    @Column(length = 10)
    private String icon;

    /** Controls the display order in the menu. Lower numbers appear first. */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private int displayOrder = 0;

    /** Whether this category is currently visible/active. */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean active = true;
}
