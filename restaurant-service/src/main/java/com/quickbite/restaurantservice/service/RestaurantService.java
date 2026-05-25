package com.quickbite.restaurantservice.service;

import com.quickbite.restaurantservice.dto.*;
import com.quickbite.restaurantservice.exception.ForbiddenException;
import com.quickbite.restaurantservice.exception.NotFoundException;
import com.quickbite.restaurantservice.model.*;
import com.quickbite.restaurantservice.repository.MenuCategoryRepository;
import com.quickbite.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuCategoryService categoryService;
    private final OperatingHourService operatingHourService;

    // ── Read ─────────────────────────────────────────────────────────────────

    @Cacheable(cacheNames = "restaurants")
    public List<RestaurantResponse> findAll() {
        return restaurantRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Cacheable(cacheNames = "menu-items")
    public List<MenuItemResponse> findAllMenuItems() {
        return restaurantRepository.findAll().stream()
                .flatMap(r -> r.getMenuItems().stream())
                .map(this::toMenuItemResponse)
                .toList();
    }

    @Cacheable(cacheNames = "menu-items-by-restaurant", key = "#restaurantId")
    public List<MenuItemResponse> findMenuItemsByRestaurantId(Long restaurantId) {
        return getRestaurant(restaurantId).getMenuItems().stream()
                .map(this::toMenuItemResponse)
                .toList();
    }

    @Cacheable(cacheNames = "restaurants-by-owner-email", key = "#ownerEmail.toLowerCase()")
    public List<RestaurantResponse> findByOwnerEmail(String ownerEmail) {
        return restaurantRepository.findByOwnerEmailIgnoreCase(ownerEmail).stream()
                .map(this::toResponse).toList();
    }

    public RestaurantResponse findByOwnerMe(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException("Not authenticated");
        }
        String principalEmail = auth.getName();
        List<Restaurant> list = restaurantRepository.findByOwnerEmailIgnoreCase(principalEmail);
        if (list.isEmpty()) {
            throw new NotFoundException("No restaurant found for owner email: " + principalEmail);
        }
        return toResponse(list.get(0));
    }

    @Cacheable(cacheNames = "restaurants-by-owner-id", key = "#ownerId")
    public List<RestaurantResponse> findByOwnerId(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId).stream()
                .map(this::toResponse).toList();
    }

    @Cacheable(cacheNames = "restaurant-by-id", key = "#id")
    public RestaurantResponse findById(Long id) {
        return toResponse(getRestaurant(id));
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    @CacheEvict(cacheNames = {
            "restaurants", "menu-items", "menu-items-by-restaurant",
            "restaurants-by-owner-email", "restaurants-by-owner-id",
            "restaurant-by-id", "menu-categories"}, allEntries = true)
    @Transactional
    public RestaurantResponse create(RestaurantRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.name())
                .address(request.address())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .ownerId(request.ownerId())
                .ownerEmail(request.ownerEmail())
                .ownerName(request.ownerName())
                .cuisineType(request.cuisineType())
                .status(RestaurantStatus.ACTIVE)
                .rating(request.rating() == null ? 0.0 : request.rating())
                .build();

        if (request.menuItems() != null) {
            request.menuItems().forEach(item -> {
                MenuItem mi = buildMenuItem(restaurant, item);
                restaurant.getMenuItems().add(mi);
            });
        }
        return toResponse(restaurantRepository.save(restaurant));
    }

    @CacheEvict(cacheNames = {
            "restaurants", "menu-items", "menu-items-by-restaurant",
            "restaurants-by-owner-email", "restaurants-by-owner-id",
            "restaurant-by-id", "menu-categories"}, allEntries = true)
    @Transactional
    public RestaurantResponse addMenuItem(Long restaurantId, MenuItemRequest request,
            Authentication auth) {
        Restaurant restaurant = getRestaurant(restaurantId);
        categoryService.assertOwnership(restaurant, auth);

        MenuItem item = buildMenuItem(restaurant, request);
        restaurant.getMenuItems().add(item);
        return toResponse(restaurantRepository.save(restaurant));
    }

    @CacheEvict(cacheNames = {
            "restaurants", "menu-items", "menu-items-by-restaurant",
            "restaurants-by-owner-email", "restaurants-by-owner-id",
            "restaurant-by-id", "menu-categories"}, allEntries = true)
    @Transactional
    public RestaurantResponse update(Long restaurantId, RestaurantUpdateRequest request, Authentication auth) {
        Restaurant restaurant = getRestaurant(restaurantId);
        categoryService.assertOwnership(restaurant, auth);

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
        if (request.description() != null) restaurant.setDescription(request.description().trim());
        if (request.minOrder() != null) restaurant.setMinOrder(request.minOrder());
        if (request.gstin() != null) restaurant.setGstin(request.gstin().trim());
        if (request.fssai() != null) restaurant.setFssai(request.fssai().trim());
        return toResponse(restaurantRepository.save(restaurant));
    }

    @CacheEvict(cacheNames = {
            "restaurants", "menu-items", "menu-items-by-restaurant",
            "restaurants-by-owner-email", "restaurants-by-owner-id",
            "restaurant-by-id", "menu-categories"}, allEntries = true)
    @Transactional
    public RestaurantResponse update(Long restaurantId, RestaurantUpdateRequest request) {
        return update(restaurantId, request, null);
    }

    @CacheEvict(cacheNames = {
            "restaurants", "menu-items", "menu-items-by-restaurant",
            "restaurants-by-owner-email", "restaurants-by-owner-id",
            "restaurant-by-id", "menu-categories"}, allEntries = true)
    @Transactional
    public RestaurantResponse updateMenuItem(Long restaurantId, Long menuItemId,
            MenuItemUpdateRequest request, Authentication auth) {
        Restaurant restaurant = getRestaurant(restaurantId);
        categoryService.assertOwnership(restaurant, auth);

        MenuItem item = restaurant.getMenuItems().stream()
                .filter(mi -> mi.getId().equals(menuItemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Menu item not found: id=" + menuItemId));

        if (request.name() != null && !request.name().isBlank()) item.setName(request.name().trim());
        if (request.description() != null) item.setDescription(request.description().trim());
        if (request.price() != null) item.setPrice(request.price());
        if (request.availability() != null) item.setAvailability(request.availability());
        if (request.isVeg() != null) item.setIsVeg(request.isVeg());
        if (request.imageUrl() != null) item.setImageUrl(request.imageUrl().isBlank() ? null : request.imageUrl().trim());
        if (request.icon() != null) item.setIcon(request.icon().isBlank() ? null : request.icon().trim());
        if (request.discountPercent() != null) item.setDiscountPercent(request.discountPercent());
        if (request.prepTimeMinutes() != null) item.setPrepTimeMinutes(request.prepTimeMinutes());
        if (request.displayOrder() != null) item.setDisplayOrder(request.displayOrder());

        // Resolve new category — validate ownership before reassigning
        if (request.categoryId() != null) {
            MenuCategory cat = categoryService.resolveCategory(request.categoryId(), restaurant);
            item.setCategory(cat);
        }

        return toResponse(restaurantRepository.save(restaurant));
    }

    @CacheEvict(cacheNames = {
            "restaurants", "menu-items", "menu-items-by-restaurant",
            "restaurants-by-owner-email", "restaurants-by-owner-id",
            "restaurant-by-id", "menu-categories"}, allEntries = true)
    @Transactional
    public RestaurantResponse removeMenuItem(Long restaurantId, Long menuItemId,
            Authentication auth) {
        Restaurant restaurant = getRestaurant(restaurantId);
        categoryService.assertOwnership(restaurant, auth);
        restaurant.getMenuItems().removeIf(mi -> mi.getId().equals(menuItemId));
        return toResponse(restaurantRepository.save(restaurant));
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private RestaurantResponse toResponse(Restaurant r) {
        List<MenuCategoryResponse> categories = r.getMenuCategories().stream()
                .map(c -> categoryService.toResponse(c, r))
                .toList();
        List<MenuItemResponse> items = r.getMenuItems().stream()
                .map(this::toMenuItemResponse)
                .toList();
        List<OperatingHourResponse> operatingHours = operatingHourService != null
                ? operatingHourService.getSchedule(r)
                : List.of();
        return new RestaurantResponse(
                r.getId(), r.getName(), r.getAddress(), r.getPhoneNumber(), r.getEmail(),
                r.getCuisineType(), r.getStatus(), r.getRating(), r.getCreatedAt(),
                r.getOwnerId(), r.getOwnerEmail(), r.getOwnerName(),
                r.getDescription(), r.getMinOrder(), r.getGstin(), r.getFssai(),
                categories, items, operatingHours);
    }

    MenuItemResponse toMenuItemResponse(MenuItem i) {
        Long catId = i.getCategory() != null ? i.getCategory().getId() : null;
        String catName = i.getCategory() != null ? i.getCategory().getName() : null;
        String catSlug = i.getCategory() != null ? i.getCategory().getSlug() : null;
        // isVeg: use stored value; true is the safe default (set by DataMigrationService)
        Boolean isVeg = i.getIsVeg() != null ? i.getIsVeg() : true;
        return new MenuItemResponse(
                i.getId(), i.getName(), i.getDescription(), i.getPrice(), i.getAvailability(),
                catId, catName, catSlug,
                isVeg, i.getImageUrl(), i.getIcon(),
                i.getDiscountPercent(), i.getPrepTimeMinutes(), i.getDisplayOrder());
    }

    // ── Builder helper ────────────────────────────────────────────────────────

    private MenuItem buildMenuItem(Restaurant restaurant, MenuItemRequest request) {
        // Resolve category: if categoryId provided, validate ownership; else assign Uncategorized
        MenuCategory category = request.categoryId() != null
                ? categoryService.resolveCategory(request.categoryId(), restaurant)
                : categoryService.getOrCreateUncategorized(restaurant);

        return MenuItem.builder()
                .name(request.name().trim())
                .description(request.description())
                .price(request.price())
                .restaurant(restaurant)
                .category(category)
                .isVeg(request.isVeg() != null ? request.isVeg() : true)
                .imageUrl(request.imageUrl())
                .icon(request.icon())
                .discountPercent(request.discountPercent())
                .prepTimeMinutes(request.prepTimeMinutes())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .availability(MenuItemAvailability.AVAILABLE)
                .build();
    }

    private Restaurant getRestaurant(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Restaurant not found: id=" + id));
    }
}
