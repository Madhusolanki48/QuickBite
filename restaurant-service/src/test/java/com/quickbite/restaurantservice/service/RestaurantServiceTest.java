package com.quickbite.restaurantservice.service;

import com.quickbite.restaurantservice.dto.MenuItemRequest;
import com.quickbite.restaurantservice.dto.RestaurantRequest;
import com.quickbite.restaurantservice.model.CuisineType;
import com.quickbite.restaurantservice.model.Restaurant;
import com.quickbite.restaurantservice.model.RestaurantStatus;
import com.quickbite.restaurantservice.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {
    @Mock private RestaurantRepository restaurantRepository;
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        restaurantService = new RestaurantService(restaurantRepository);
    }

    @Test
    void createAddsRestaurantWithMenuItems() {
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant restaurant = invocation.getArgument(0);
            restaurant.setId(7L);
            return restaurant;
        });

        var response = restaurantService.create(new RestaurantRequest(
                "Sita Ram Cafe",
                "Navrangpura",
                "9999999999",
                "hello@quickbite.com",
                CuisineType.ITALIAN,
                4.7,
                42L,
                "owner@quickbite.com",
                "Sita Owner",
                List.of(new MenuItemRequest("Garden Pizza", "Veg pizza", 359.0))
        ));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.menuItems()).hasSize(1);
        assertThat(response.status()).isEqualTo(RestaurantStatus.ACTIVE);
    }
}
