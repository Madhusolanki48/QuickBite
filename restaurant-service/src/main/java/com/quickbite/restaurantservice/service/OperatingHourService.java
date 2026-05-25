package com.quickbite.restaurantservice.service;

import com.quickbite.restaurantservice.dto.OperatingHourRequest;
import com.quickbite.restaurantservice.dto.OperatingHourResponse;
import com.quickbite.restaurantservice.exception.NotFoundException;
import com.quickbite.restaurantservice.model.OperatingHour;
import com.quickbite.restaurantservice.model.Restaurant;
import com.quickbite.restaurantservice.repository.OperatingHourRepository;
import com.quickbite.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OperatingHourService {

    private static final List<String> ORDERED_DAYS = List.of(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    );

    private final OperatingHourRepository operatingHourRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryService categoryService;

    @Transactional
    public List<OperatingHourResponse> getSchedule(Long restaurantId) {
        Restaurant restaurant = getRestaurant(restaurantId);
        return getSchedule(restaurant);
    }

    @Transactional
    public List<OperatingHourResponse> getSchedule(Restaurant restaurant) {
        List<OperatingHour> existing = operatingHourRepository.findByRestaurantOrderByDayAsc(restaurant);
        if (existing.isEmpty()) {
            seedDefaults(restaurant);
            existing = operatingHourRepository.findByRestaurantOrderByDayAsc(restaurant);
        }
        return sortAndMap(existing);
    }

    @CacheEvict(cacheNames = {
            "restaurants", "restaurant-by-id", "restaurants-by-owner-email", "restaurants-by-owner-id"
    }, allEntries = true)
    @Transactional
    public List<OperatingHourResponse> saveSchedule(Long restaurantId, List<OperatingHourRequest> requests,
            Authentication auth) {
        Restaurant restaurant = getRestaurant(restaurantId);
        categoryService.assertOwnership(restaurant, auth);

        if (requests != null) {
            for (OperatingHourRequest req : requests) {
                if (req == null || req.day() == null || req.day().isBlank()) continue;
                String normalizedDay = normalizeDay(req.day());
                OperatingHour hour = operatingHourRepository.findByRestaurantAndDay(restaurant, normalizedDay)
                        .orElseGet(() -> OperatingHour.builder()
                                .restaurant(restaurant)
                                .day(normalizedDay)
                                .build());

                hour.setOpenTime(req.openTime() != null ? req.openTime().trim() : "10:00");
                hour.setCloseTime(req.closeTime() != null ? req.closeTime().trim() : "23:00");
                hour.setOpenToday(req.openToday());
                operatingHourRepository.save(hour);
            }
        }

        List<OperatingHour> updated = operatingHourRepository.findByRestaurantOrderByDayAsc(restaurant);
        return sortAndMap(updated);
    }

    @Transactional
    public void seedDefaultsIfEmpty(Restaurant restaurant) {
        if (!operatingHourRepository.existsByRestaurant(restaurant)) {
            seedDefaults(restaurant);
        }
    }

    private void seedDefaults(Restaurant restaurant) {
        for (String day : ORDERED_DAYS) {
            boolean isSunday = "Sunday".equalsIgnoreCase(day);
            OperatingHour hour = OperatingHour.builder()
                    .restaurant(restaurant)
                    .day(day)
                    .openTime(isSunday ? "11:00" : "10:00")
                    .closeTime(isSunday ? "22:00" : "23:00")
                    .openToday(!isSunday)
                    .build();
            operatingHourRepository.save(hour);
        }
    }

    private List<OperatingHourResponse> sortAndMap(List<OperatingHour> hours) {
        Map<String, OperatingHour> byDay = new HashMap<>();
        for (OperatingHour h : hours) {
            byDay.put(h.getDay().toLowerCase(), h);
        }

        List<OperatingHourResponse> responses = new ArrayList<>();
        for (String canonicalDay : ORDERED_DAYS) {
            OperatingHour h = byDay.get(canonicalDay.toLowerCase());
            if (h != null) {
                responses.add(new OperatingHourResponse(
                        h.getId(), h.getDay(), h.getOpenTime(), h.getCloseTime(), h.isOpenToday()
                ));
            }
        }
        return responses;
    }

    private String normalizeDay(String day) {
        for (String canonical : ORDERED_DAYS) {
            if (canonical.equalsIgnoreCase(day.trim())) {
                return canonical;
            }
        }
        return day.trim();
    }

    private Restaurant getRestaurant(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Restaurant not found: id=" + id));
    }
}
