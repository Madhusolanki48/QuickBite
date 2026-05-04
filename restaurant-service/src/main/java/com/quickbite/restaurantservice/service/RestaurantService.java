package com.quickbite.restaurantservice.service;

import com.quickbite.restaurantservice.dto.*;
import com.quickbite.restaurantservice.exception.NotFoundException;
import com.quickbite.restaurantservice.model.*;
import com.quickbite.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    @Cacheable(cacheNames = "restaurants")
    public List<RestaurantResponse> findAll() { return restaurantRepository.findAll().stream().map(this::toResponse).toList(); }
    @Cacheable(cacheNames = "menu-items")
    public List<MenuItemResponse> findAllMenuItems() {
        return restaurantRepository.findAll().stream()
                .flatMap(restaurant -> restaurant.getMenuItems().stream())
                .map(item -> new MenuItemResponse(item.getId(), item.getName(), item.getDescription(), item.getPrice(), item.getAvailability()))
                .toList();
    }
    @Cacheable(cacheNames = "menu-items-by-restaurant", key = "#restaurantId")
    public List<MenuItemResponse> findMenuItemsByRestaurantId(Long restaurantId) {
        return getRestaurant(restaurantId).getMenuItems().stream()
                .map(item -> new MenuItemResponse(item.getId(), item.getName(), item.getDescription(), item.getPrice(), item.getAvailability()))
                .toList();
    }
    @Cacheable(cacheNames = "restaurants-by-owner-email", key = "#ownerEmail.toLowerCase()")
    public List<RestaurantResponse> findByOwnerEmail(String ownerEmail) { return restaurantRepository.findByOwnerEmailIgnoreCase(ownerEmail).stream().map(this::toResponse).toList(); }
    @Cacheable(cacheNames = "restaurants-by-owner-id", key = "#ownerId")
    public List<RestaurantResponse> findByOwnerId(Long ownerId) { return restaurantRepository.findByOwnerId(ownerId).stream().map(this::toResponse).toList(); }
    @Cacheable(cacheNames = "restaurant-by-id", key = "#id")
    public RestaurantResponse findById(Long id) { return toResponse(getRestaurant(id)); }
    @CacheEvict(cacheNames = { "restaurants", "menu-items", "menu-items-by-restaurant", "restaurants-by-owner-email", "restaurants-by-owner-id", "restaurant-by-id" }, allEntries = true)
    @Transactional
    public RestaurantResponse create(RestaurantRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.name()).address(request.address()).phoneNumber(request.phoneNumber()).email(request.email())
                .ownerId(request.ownerId())
                .ownerEmail(request.ownerEmail())
                .ownerName(request.ownerName())
                .cuisineType(request.cuisineType()).status(RestaurantStatus.ACTIVE).rating(request.rating() == null ? 0.0 : request.rating())
                .build();
        if (request.menuItems() != null) {
            request.menuItems().forEach(item -> restaurant.getMenuItems().add(buildMenuItem(restaurant, item.name(), item.description(), item.price(), MenuItemAvailability.AVAILABLE)));
        }
        return toResponse(restaurantRepository.save(restaurant));
    }
    @CacheEvict(cacheNames = { "restaurants", "menu-items", "menu-items-by-restaurant", "restaurants-by-owner-email", "restaurants-by-owner-id", "restaurant-by-id" }, allEntries = true)
    @Transactional
    public RestaurantResponse addMenuItem(Long restaurantId, MenuItemRequest request) {
        Restaurant restaurant = getRestaurant(restaurantId);
        restaurant.getMenuItems().add(buildMenuItem(restaurant, request.name(), request.description(), request.price(), MenuItemAvailability.AVAILABLE));
        return toResponse(restaurantRepository.save(restaurant));
    }
    @CacheEvict(cacheNames = { "restaurants", "menu-items", "menu-items-by-restaurant", "restaurants-by-owner-email", "restaurants-by-owner-id", "restaurant-by-id" }, allEntries = true)
    @Transactional
    public RestaurantResponse update(Long restaurantId, RestaurantUpdateRequest request) {
        Restaurant restaurant = getRestaurant(restaurantId);
        if (request.name() != null && !request.name().isBlank()) restaurant.setName(request.name().trim());
        if (request.address() != null && !request.address().isBlank()) restaurant.setAddress(request.address().trim());
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) restaurant.setPhoneNumber(request.phoneNumber().trim());
        if (request.email() != null && !request.email().isBlank()) restaurant.setEmail(request.email().trim());
        if (request.cuisineType() != null) restaurant.setCuisineType(request.cuisineType());
        if (request.status() != null) restaurant.setStatus(request.status());
        if (request.rating() != null) restaurant.setRating(request.rating());
        if (request.ownerId() != null) restaurant.setOwnerId(request.ownerId());
        if (request.ownerEmail() != null && !request.ownerEmail().isBlank()) restaurant.setOwnerEmail(request.ownerEmail().trim());
        if (request.ownerName() != null && !request.ownerName().isBlank()) restaurant.setOwnerName(request.ownerName().trim());
        return toResponse(restaurantRepository.save(restaurant));
    }
    @CacheEvict(cacheNames = { "restaurants", "menu-items", "menu-items-by-restaurant", "restaurants-by-owner-email", "restaurants-by-owner-id", "restaurant-by-id" }, allEntries = true)
    @Transactional
    public RestaurantResponse updateMenuItem(Long restaurantId, Long menuItemId, MenuItemUpdateRequest request) {
        Restaurant restaurant = getRestaurant(restaurantId);
        MenuItem item = restaurant.getMenuItems().stream().filter(menuItem -> menuItem.getId().equals(menuItemId)).findFirst().orElseThrow(() -> new NotFoundException("Menu item not found"));
        if (request.name() != null && !request.name().isBlank()) item.setName(request.name().trim());
        if (request.description() != null) item.setDescription(request.description().trim());
        if (request.price() != null) item.setPrice(request.price());
        if (request.availability() != null) item.setAvailability(request.availability());
        return toResponse(restaurantRepository.save(restaurant));
    }
    @CacheEvict(cacheNames = { "restaurants", "menu-items", "menu-items-by-restaurant", "restaurants-by-owner-email", "restaurants-by-owner-id", "restaurant-by-id" }, allEntries = true)
    @Transactional
    public RestaurantResponse removeMenuItem(Long restaurantId, Long menuItemId) {
        Restaurant restaurant = getRestaurant(restaurantId);
        restaurant.getMenuItems().removeIf(menuItem -> menuItem.getId().equals(menuItemId));
        return toResponse(restaurantRepository.save(restaurant));
    }
    private Restaurant getRestaurant(Long id) { return restaurantRepository.findById(id).orElseThrow(() -> new NotFoundException("Restaurant not found")); }
    private RestaurantResponse toResponse(Restaurant r) {
        return new RestaurantResponse(r.getId(), r.getName(), r.getAddress(), r.getPhoneNumber(), r.getEmail(), r.getCuisineType(), r.getStatus(), r.getRating(), r.getCreatedAt(), r.getOwnerId(), r.getOwnerEmail(), r.getOwnerName(),
                r.getMenuItems().stream().map(i -> new MenuItemResponse(i.getId(), i.getName(), i.getDescription(), i.getPrice(), i.getAvailability())).toList());
    }
    private MenuItem buildMenuItem(Restaurant restaurant, String name, String description, double price, MenuItemAvailability availability) {
        return MenuItem.builder().name(name).description(description).price(price).restaurant(restaurant).availability(availability).build();
    }
}
