package com.quickbite.restaurantservice.controller;

import com.quickbite.restaurantservice.dto.internal.ItemValidationResponse;
import com.quickbite.restaurantservice.dto.internal.ValidateItemsRequest;
import com.quickbite.restaurantservice.model.MenuCategory;
import com.quickbite.restaurantservice.model.MenuItem;
import com.quickbite.restaurantservice.model.MenuItemAvailability;
import com.quickbite.restaurantservice.model.Restaurant;
import com.quickbite.restaurantservice.model.RestaurantStatus;
import com.quickbite.restaurantservice.repository.MenuItemRepository;
import com.quickbite.restaurantservice.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantInternalControllerTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private RestaurantInternalController controller;

    private Restaurant restaurant;
    private MenuCategory category;
    private MenuItem item1;
    private MenuItem item2;

    @BeforeEach
    void setUp() {
        restaurant = Restaurant.builder()
                .id(1L)
                .name("Burger Palace")
                .status(RestaurantStatus.ACTIVE)
                .build();

        category = MenuCategory.builder()
                .id(10L)
                .name("Burgers")
                .active(true)
                .restaurant(restaurant)
                .build();

        item1 = MenuItem.builder()
                .id(101L)
                .name("Cheese Burger")
                .price(200.0)
                .discountPercent(10)
                .availability(MenuItemAvailability.AVAILABLE)
                .restaurant(restaurant)
                .category(category)
                .build();

        item2 = MenuItem.builder()
                .id(102L)
                .name("Bacon Burger")
                .price(250.0)
                .discountPercent(0)
                .availability(MenuItemAvailability.UNAVAILABLE)
                .restaurant(restaurant)
                .category(category)
                .build();
    }

    @Test
    void validateItems_allValid_calculatesEffectivePrice() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findById(101L)).thenReturn(Optional.of(item1));

        ValidateItemsRequest req = new ValidateItemsRequest(1L, List.of(101L));
        ResponseEntity<ItemValidationResponse> response = controller.validateItems(req);

        assertEquals(200, response.getStatusCode().value());
        ItemValidationResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.restaurantOpen());
        assertTrue(body.errors().isEmpty());
        assertEquals(1, body.items().size());
        assertEquals(180.0, body.items().get(0).effectivePrice());
        assertEquals(200.0, body.items().get(0).price());
    }

    @Test
    void validateItems_closedRestaurant_reportsError() {
        restaurant.setStatus(RestaurantStatus.CLOSED);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findById(101L)).thenReturn(Optional.of(item1));

        ValidateItemsRequest req = new ValidateItemsRequest(1L, List.of(101L));
        ResponseEntity<ItemValidationResponse> response = controller.validateItems(req);

        ItemValidationResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.restaurantOpen());
        assertFalse(body.errors().isEmpty());
        assertTrue(body.errors().get(0).contains("closed"));
    }

    @Test
    void validateItems_unavailableItem_reportsError() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findById(102L)).thenReturn(Optional.of(item2));

        ValidateItemsRequest req = new ValidateItemsRequest(1L, List.of(102L));
        ResponseEntity<ItemValidationResponse> response = controller.validateItems(req);

        ItemValidationResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.errors().isEmpty());
        assertTrue(body.errors().get(0).contains("unavailable"));
    }

    @Test
    void validateItems_itemBelongsToDifferentRestaurant_reportsError() {
        Restaurant otherRestaurant = Restaurant.builder().id(2L).name("Pizza Hut").build();
        item1.setRestaurant(otherRestaurant);

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findById(101L)).thenReturn(Optional.of(item1));

        ValidateItemsRequest req = new ValidateItemsRequest(1L, List.of(101L));
        ResponseEntity<ItemValidationResponse> response = controller.validateItems(req);

        ItemValidationResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.errors().isEmpty());
        assertTrue(body.errors().get(0).contains("does not belong to restaurant"));
    }
}
