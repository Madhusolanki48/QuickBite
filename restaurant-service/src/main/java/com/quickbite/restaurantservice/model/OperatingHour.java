package com.quickbite.restaurantservice.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a single day's operating hours for a restaurant.
 *
 * Each restaurant has exactly 7 rows — one per day of the week (Monday through Sunday).
 * Rows are created on first save (or by DataMigrationService for existing restaurants).
 *
 * Uniqueness is enforced by the composite constraint (restaurant_id, day).
 */
@Entity
@Table(
    name = "operating_hour",
    uniqueConstraints = @UniqueConstraint(columnNames = {"restaurant_id", "day"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatingHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The restaurant this schedule row belongs to. Always non-null. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    /**
     * Day of the week, e.g. "Monday", "Tuesday", …, "Sunday".
     * Stored as a fixed English string to match the frontend OperatingHour model.
     */
    @Column(nullable = false, length = 12)
    private String day;

    /** Opening time in HH:mm format, e.g. "10:00". */
    @Column(nullable = false, length = 5)
    private String openTime;

    /** Closing time in HH:mm format, e.g. "23:00". */
    @Column(nullable = false, length = 5)
    private String closeTime;

    /** Whether the restaurant operates on this day. */
    @Column(nullable = false)
    private boolean openToday;
}
