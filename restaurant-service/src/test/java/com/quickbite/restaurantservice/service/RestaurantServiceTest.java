package com.quickbite.restaurantservice.service;

import com.quickbite.restaurantservice.dto.MenuItemRequest;
import com.quickbite.restaurantservice.dto.RestaurantRequest;
import com.quickbite.restaurantservice.dto.RestaurantUpdateRequest;
import com.quickbite.restaurantservice.model.CuisineType;
import com.quickbite.restaurantservice.model.Restaurant;
import com.quickbite.restaurantservice.model.RestaurantStatus;
import com.quickbite.restaurantservice.model.MenuCategory;
import com.quickbite.restaurantservice.repository.MenuCategoryRepository;
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
    @Mock private MenuCategoryRepository menuCategoryRepository;
    @Mock private MenuCategoryService categoryService;
    @Mock private OperatingHourService operatingHourService;
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        restaurantService = new RestaurantService(restaurantRepository, menuCategoryRepository, categoryService, operatingHourService);
    }

    @Test
    void createAddsRestaurantWithMenuItems() {
        when(categoryService.getOrCreateUncategorized(any())).thenReturn(
                MenuCategory.builder().id(101L).name("Uncategorized").slug("uncategorized").build());
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

    @Test
    void updatePersistsNewFieldsAndEnforcesOwnership() {
        Restaurant restaurant = Restaurant.builder()
                .id(10L)
                .name("Old Name")
                .address("Old Address")
                .phoneNumber("1111111111")
                .email("test@example.com")
                .ownerEmail("ownerA@quickbite.com")
                .cuisineType(CuisineType.INDIAN)
                .status(RestaurantStatus.ACTIVE)
                .build();

        when(restaurantRepository.findById(10L)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        org.springframework.security.core.Authentication auth =
                org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);

        var updateReq = new RestaurantUpdateRequest(
                "New Name", "New Address", "2222222222", "new@example.com",
                CuisineType.CHINESE, RestaurantStatus.ACTIVE, 4.5, 99L,
                "ownerA@quickbite.com", "Owner A",
                "Authentic taste", 200, "27ABCDE1234F1Z5", "12345678901234"
        );

        var response = restaurantService.update(10L, updateReq, auth);

        org.mockito.Mockito.verify(categoryService).assertOwnership(restaurant, auth);
        assertThat(response.description()).isEqualTo("Authentic taste");
        assertThat(response.minOrder()).isEqualTo(200);
        assertThat(response.gstin()).isEqualTo("27ABCDE1234F1Z5");
        assertThat(response.fssai()).isEqualTo("12345678901234");
    }

    @Test
    void ownerCannotUpdateOthersRestaurant() {
        Restaurant restaurant = Restaurant.builder()
                .id(10L)
                .name("Owner B Restaurant")
                .ownerEmail("ownerB@quickbite.com")
                .build();

        when(restaurantRepository.findById(10L)).thenReturn(Optional.of(restaurant));

        org.springframework.security.core.Authentication auth =
                org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
        org.mockito.Mockito.doThrow(new com.quickbite.restaurantservice.exception.ForbiddenException("Access denied"))
                .when(categoryService).assertOwnership(restaurant, auth);

        var updateReq = new RestaurantUpdateRequest(
                "Hacked Name", "Address", "2222222222", "new@example.com",
                CuisineType.CHINESE, RestaurantStatus.ACTIVE, 4.5, 99L,
                "ownerA@quickbite.com", "Owner A",
                "Hacked", 200, "GSTIN", "FSSAI"
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                com.quickbite.restaurantservice.exception.ForbiddenException.class,
                () -> restaurantService.update(10L, updateReq, auth)
        );
    }

    @Test
    void findByOwnerMe_returnsOwnerRestaurant() {
        Restaurant restaurant = Restaurant.builder()
                .id(10L)
                .name("Owner A Restaurant")
                .ownerEmail("ownerA@quickbite.com")
                .cuisineType(CuisineType.INDIAN)
                .status(RestaurantStatus.ACTIVE)
                .build();

        when(restaurantRepository.findByOwnerEmailIgnoreCase("ownerA@quickbite.com"))
                .thenReturn(List.of(restaurant));

        org.springframework.security.core.Authentication auth =
                org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("ownerA@quickbite.com");

        var response = restaurantService.findByOwnerMe(auth);
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Owner A Restaurant");
    }
}
