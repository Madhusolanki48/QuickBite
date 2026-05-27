package com.quickbite.restaurantservice.service;

import com.quickbite.restaurantservice.dto.MenuCategoryRequest;
import com.quickbite.restaurantservice.exception.ForbiddenException;
import com.quickbite.restaurantservice.model.MenuCategory;
import com.quickbite.restaurantservice.model.Restaurant;
import com.quickbite.restaurantservice.repository.MenuCategoryRepository;
import com.quickbite.restaurantservice.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuCategoryServiceTest {

    @Mock private RestaurantRepository restaurantRepository;
    @Mock private MenuCategoryRepository menuCategoryRepository;
    private MenuCategoryService categoryService;

    private Restaurant sampleRestaurant;

    @BeforeEach
    void setUp() {
        categoryService = new MenuCategoryService(restaurantRepository, menuCategoryRepository);
        sampleRestaurant = Restaurant.builder()
                .id(10L)
                .name("Test Bistro")
                .ownerEmail("owner@bistro.com")
                .build();
    }

    @Test
    void createCategorySuccessfullyByOwner() {
        when(restaurantRepository.findById(10L)).thenReturn(Optional.of(sampleRestaurant));
        when(menuCategoryRepository.findByRestaurantAndSlug(sampleRestaurant, "beverages")).thenReturn(Optional.empty());
        when(menuCategoryRepository.save(any(MenuCategory.class))).thenAnswer(inv -> {
            MenuCategory cat = inv.getArgument(0);
            cat.setId(101L);
            return cat;
        });

        var auth = new UsernamePasswordAuthenticationToken(
                "owner@bistro.com", null, List.of(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER")));

        var resp = categoryService.create(10L, new MenuCategoryRequest("Beverages", "drink", 1, true), auth);

        assertThat(resp.id()).isEqualTo(101L);
        assertThat(resp.name()).isEqualTo("Beverages");
        assertThat(resp.slug()).isEqualTo("beverages");
    }

    @Test
    void createCategoryFailsForDifferentOwner() {
        when(restaurantRepository.findById(10L)).thenReturn(Optional.of(sampleRestaurant));

        var auth = new UsernamePasswordAuthenticationToken(
                "other@bistro.com", null, List.of(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER")));

        assertThatThrownBy(() -> categoryService.create(10L, new MenuCategoryRequest("Beverages", "drink", 1, true), auth))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Access denied: you do not own restaurant id=");
    }

    @Test
    void createCategoryAllowedForAdmin() {
        when(restaurantRepository.findById(10L)).thenReturn(Optional.of(sampleRestaurant));
        when(menuCategoryRepository.findByRestaurantAndSlug(sampleRestaurant, "desserts")).thenReturn(Optional.empty());
        when(menuCategoryRepository.save(any(MenuCategory.class))).thenAnswer(inv -> {
            MenuCategory cat = inv.getArgument(0);
            cat.setId(102L);
            return cat;
        });

        var auth = new UsernamePasswordAuthenticationToken(
                "admin@quickbite.com", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        var resp = categoryService.create(10L, new MenuCategoryRequest("Desserts", "cake", 2, true), auth);

        assertThat(resp.id()).isEqualTo(102L);
        assertThat(resp.name()).isEqualTo("Desserts");
    }

    @Test
    void getOrCreateUncategorizedCategoryReturnsExisting() {
        MenuCategory existing = MenuCategory.builder().id(999L).name("Uncategorized").slug("uncategorized").build();
        when(menuCategoryRepository.findByRestaurantAndSlug(sampleRestaurant, "uncategorized"))
                .thenReturn(Optional.of(existing));

        MenuCategory result = categoryService.getOrCreateUncategorizedCategory(sampleRestaurant);
        assertThat(result.getId()).isEqualTo(999L);
    }
}
