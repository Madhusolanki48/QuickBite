package com.quickbite.restaurantservice.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a single food item in a restaurant's menu.
 *
 * Relationships:
 *   MenuItem → Restaurant  (ManyToOne, non-null)
 *   MenuItem → MenuCategory (ManyToOne, nullable for safe migration of existing rows)
 *
 * Migration note:
 *   - category: existing rows will have category_id = NULL until DataMigrationService runs
 *     on application start-up, which assigns them to a per-restaurant "Uncategorized" category.
 *   - isVeg: stored as nullable Boolean so existing rows are not forced to false (Java primitive default).
 *     DataMigrationService sets is_veg = TRUE for any row where is_veg IS NULL on startup.
 *     Application code must treat null as true via MenuItemService.resolveIsVeg().
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuItemAvailability availability;

    /** The restaurant this item belongs to. Always non-null. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    /**
     * The category this item belongs to. Nullable only during migration window.
     * DataMigrationService assigns "Uncategorized" to any null category on startup.
     * All items will have a category after the first application start post-migration.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private MenuCategory category;

    /**
     * Whether the item is vegetarian.
     * Uses Boolean wrapper (not primitive) so that existing database rows with NULL are safe.
     * DataMigrationService sets NULL → TRUE on startup (safe default for existing data).
     */
    @Column(name = "is_veg")
    private Boolean isVeg;

    /** Optional URL to the item image (hosted or uploaded). */
    @Column(length = 500)
    private String imageUrl;

    /** Optional emoji icon, e.g. "🍔". */
    @Column(length = 10)
    private String icon;

    /** Optional discount percentage (0–100). Null means no discount. */
    @Column
    private Integer discountPercent;

    /** Estimated preparation time in minutes. */
    @Column
    private Integer prepTimeMinutes;

    /** Controls sort order within a category. Lower numbers appear first. */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private int displayOrder = 0;

    @PrePersist
    void onCreate() {
        if (availability == null)
            availability = MenuItemAvailability.AVAILABLE;
        if (isVeg == null)
            isVeg = true;
    }
}
