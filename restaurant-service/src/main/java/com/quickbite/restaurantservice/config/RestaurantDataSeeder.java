package com.quickbite.restaurantservice.config;

import com.quickbite.restaurantservice.model.*;
import com.quickbite.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestaurantDataSeeder implements CommandLineRunner {

    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking and validating restaurant catalogue...");
        associateOwner("Urban Bites", "owner.urbanbites@quickbite.com", "Urban Bites Owner");
        associateOwner("Crust & Co.", "owner.crustco@quickbite.com", "Crust & Co. Owner");
        associateOwner("Royal Tadka", "owner.royaltadka@quickbite.com", "Royal Tadka Owner");
        associateOwner("Wok & Bowl", "owner.wokbowl@quickbite.com", "Wok & Bowl Owner");
        associateOwner("Green Spoon", "owner.greenspoon@quickbite.com", "Green Spoon Owner");
        associateOwner("The Food Yard", "owner.foodyard@quickbite.com", "The Food Yard Owner");
        log.info("Restaurants verified: count={}", restaurantRepository.count());
    }

    private void associateOwner(String restaurantName, String ownerEmail, String ownerName) {
        restaurantRepository.findByNameIgnoreCase(restaurantName).ifPresent(r -> {
            r.setOwnerEmail(ownerEmail.toLowerCase());
            r.setOwnerName(ownerName);
            restaurantRepository.save(r);
            log.info("Associated restaurant '{}' with owner '{}'", restaurantName, ownerEmail);
        });
    }
}
