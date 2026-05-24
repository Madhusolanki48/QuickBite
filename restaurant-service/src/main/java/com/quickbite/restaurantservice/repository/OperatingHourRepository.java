package com.quickbite.restaurantservice.repository;

import com.quickbite.restaurantservice.model.OperatingHour;
import com.quickbite.restaurantservice.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OperatingHourRepository extends JpaRepository<OperatingHour, Long> {

    /** Returns all hours for a restaurant, ordered alphabetically by day name. */
    List<OperatingHour> findByRestaurantOrderByDayAsc(Restaurant restaurant);

    /** Returns the operating-hour row for a specific (restaurant, day) combination. */
    Optional<OperatingHour> findByRestaurantAndDay(Restaurant restaurant, String day);

    /** Removes all operating-hour rows for a restaurant (used before batch re-insert). */
    void deleteAllByRestaurant(Restaurant restaurant);

    /** Returns true if any operating-hour rows already exist for this restaurant. */
    boolean existsByRestaurant(Restaurant restaurant);
}
