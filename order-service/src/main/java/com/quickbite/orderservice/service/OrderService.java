package com.quickbite.orderservice.service;

import com.quickbite.orderservice.client.DeliveryServiceClient;
import com.quickbite.orderservice.config.PricingProperties;
import com.quickbite.orderservice.dto.*;
import com.quickbite.orderservice.dto.internal.ItemValidationResponse;
import com.quickbite.orderservice.dto.internal.ValidateItemsRequest;
import com.quickbite.orderservice.dto.internal.ValidatedItemDto;
import com.quickbite.orderservice.exception.NotFoundException;
import com.quickbite.orderservice.model.*;
import com.quickbite.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final MailService mailService;
    private final WebClient webClient;
    private final PricingProperties pricingProperties;
    private final String restaurantServiceUrl;
    private final DeliveryServiceClient deliveryServiceClient;
    private final String internalServiceKey;
    private final com.quickbite.orderservice.messaging.OrderEventProducer eventProducer;

    public OrderService(
            OrderRepository orderRepository,
            MailService mailService,
            WebClient.Builder webClientBuilder,
            PricingProperties pricingProperties,
            DeliveryServiceClient deliveryServiceClient,
            com.quickbite.orderservice.messaging.OrderEventProducer eventProducer,
            @Value("${services.restaurant-service.url:http://localhost:8082}") String restaurantServiceUrl,
            @Value("${quickbite.internal.service-key:quickbite-internal-secret-token}") String internalServiceKey) {
        this.orderRepository = orderRepository;
        this.mailService = mailService;
        this.webClient = webClientBuilder.build();
        this.pricingProperties = pricingProperties;
        this.deliveryServiceClient = deliveryServiceClient;
        this.eventProducer = eventProducer;
        this.restaurantServiceUrl = restaurantServiceUrl;
        this.internalServiceKey = internalServiceKey;
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    public List<OrderResponse> findByRestaurantId(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream().map(this::toResponse).toList();
    }

    public List<OrderResponse> findByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(email).stream().map(this::toResponse).toList();
    }

    public OrderResponse findById(Long id) {
        return toResponse(orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found")));
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        if (request.restaurantId() == null || request.restaurantId() <= 0) {
            throw new IllegalArgumentException("A valid Restaurant ID is required");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        List<Long> menuItemIds = request.items().stream()
                .map(OrderItemRequest::menuItemId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        // Try to validate items with restaurant-service; fall back gracefully if unavailable
        Map<Long, ValidatedItemDto> validatedMap = java.util.Collections.emptyMap();
        boolean validationSucceeded = false;
        if (!menuItemIds.isEmpty()) {
            try {
                List<ValidatedItemDto> validatedItems = validateItemsWithRestaurantService(request.restaurantId(), menuItemIds);
                validatedMap = validatedItems.stream()
                        .collect(Collectors.toMap(ValidatedItemDto::id, v -> v));
                validationSucceeded = true;
            } catch (Exception ex) {
                log.warn("Item validation via restaurant-service failed, accepting order with frontend-provided prices: {}", ex.getMessage());
            }
        }

        FoodOrder order = FoodOrder.builder()
                .customerId(request.customerId())
                .restaurantId(request.restaurantId())
                .customerEmail(request.customerEmail() == null ? null : request.customerEmail().trim().toLowerCase())
                .customerName(request.customerName() != null ? request.customerName().trim() : null)
                .customerPhone(request.customerPhone() != null ? request.customerPhone().trim() : null)
                .deliveryAddress(request.deliveryAddress() != null && !request.deliveryAddress().isBlank()
                        ? request.deliveryAddress().trim()
                        : "Address on file")
                .note(request.note() != null ? request.note().trim() : null)
                .deliveryAgentStatus(DeliveryStatus.UNASSIGNED)
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(normalizePaymentStatus(request.paymentStatus()))
                .build();

        for (OrderItemRequest itemReq : request.items()) {
            ValidatedItemDto validated = validationSucceeded ? validatedMap.get(itemReq.menuItemId()) : null;
            double price = (validated != null) ? validated.effectivePrice()
                    : (itemReq.unitPrice() != null && itemReq.unitPrice() > 0 ? itemReq.unitPrice() : 0.0);
            String name = (validated != null) ? validated.name()
                    : (itemReq.itemName() != null ? itemReq.itemName() : "Item");
            if (validated != null && (!validated.available() || !validated.active())) {
                throw new IllegalArgumentException("Menu item '" + validated.name() + "' is currently unavailable");
            }
            FoodOrderItem item = FoodOrderItem.builder()
                    .menuItemId(itemReq.menuItemId() != null && itemReq.menuItemId() > 0 ? itemReq.menuItemId() : null)
                    .itemName(name)
                    .quantity(itemReq.quantity())
                    .unitPrice(price)
                    .build();
            order.getItems().add(item);
        }

        double subtotal = round(order.getItems().stream().mapToDouble(i -> i.getUnitPrice() * i.getQuantity()).sum());

        double discount = 0.0;
        if (request.promoCode() != null && request.promoCode().trim().equalsIgnoreCase("FOOD10")) {
            if (subtotal >= 100.0) {
                discount = 50.0;
            }
        }
        discount = Math.min(discount, subtotal);

        double deliveryFee = pricingProperties.deliveryFee();
        double gst = round(subtotal * pricingProperties.gstRate());
        double totalAmount = round(Math.max(0.0, subtotal - discount) + deliveryFee + gst);

        order.setSubtotal(subtotal);
        order.setDiscountAmount(discount);
        order.setDeliveryFee(deliveryFee);
        order.setGst(gst);
        order.setTotalAmount(totalAmount);

        FoodOrder saved = orderRepository.save(order);
        OrderResponse response = toResponse(saved);
        eventProducer.publishOrderCreated(saved, response);
        try {
            mailService.sendOrderConfirmation(saved);
        } catch (RuntimeException ex) {
            log.warn("Failed to send order confirmation email: {}", ex.getMessage());
        }
        return response;
    }

    @Transactional
    public OrderResponse assignDelivery(Long orderId, AssignDeliveryRequest request, Authentication auth) {
        FoodOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot assign delivery to an order that is " + order.getOrderStatus());
        }

        // Authorization check: Owner must own the restaurant associated with the order (or Admin)
        assertRestaurantOwnerOrAdmin(order.getRestaurantId(), auth);

        // Authoritative rider lookup and validation via delivery-service internal API
        DeliveryServiceClient.InternalRiderDto rider = deliveryServiceClient.validateAndGetRider(request.riderId());

        // Update persistent fields strictly from authoritative rider payload
        order.setDeliveryAgentId(rider.userId());
        order.setDeliveryAgentName(rider.fullName());
        order.setDeliveryAgentEmail(rider.email());
        order.setDeliveryAgentPhone(rider.phoneNumber());
        order.setDeliveryAgentStatus(DeliveryStatus.ASSIGNED);

        FoodOrder saved = orderRepository.save(order);

        // Notify delivery-service to record assignment
        deliveryServiceClient.recordAssignment(
                saved.getId(),
                rider.userId(),
                rider.fullName(),
                rider.phoneNumber(),
                saved.getDeliveryAddress()
        );

        log.info("Assigned rider {} ({}) to order {}", rider.userId(), rider.fullName(), saved.getId());
        OrderResponse response = toResponse(saved);
        eventProducer.publishOrderAssigned(saved, response);
        return response;
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus, Authentication auth) {
        return updateStatus(id, newStatus, auth, null);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus, Authentication auth, String reason) {
        FoodOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + id));

        validateStatusTransition(order, newStatus, auth);

        order.setOrderStatus(newStatus);

        // Status coupling rules:
        // Rider pickup: delivery status PICKED_UP, order status OUT_FOR_DELIVERY
        // Delivery completed: delivery status DELIVERED, order status DELIVERED
        if (newStatus == OrderStatus.OUT_FOR_DELIVERY) {
            order.setDeliveryAgentStatus(DeliveryStatus.PICKED_UP);
        } else if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveryAgentStatus(DeliveryStatus.DELIVERED);
        } else if (newStatus == OrderStatus.CANCELLED) {
            order.setDeliveryAgentStatus(DeliveryStatus.CANCELLED);
            if (reason != null && !reason.isBlank()) {
                order.setNote("Cancelled: " + reason.trim());
            } else if (order.getNote() == null || !order.getNote().startsWith("Cancelled")) {
                order.setNote("Cancelled: Kitchen capacity / Ingredients unavailable");
            }
            deliveryServiceClient.cancelAssignment(id);
        }

        FoodOrder saved = orderRepository.save(order);
        OrderResponse response = toResponse(saved);
        eventProducer.publishOrderStatusChanged(saved, response);
        return response;
    }

    @Transactional
    public OrderResponse updatePaymentStatus(Long id, PaymentStatus status) {
        FoodOrder order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        order.setPaymentStatus(status);
        return toResponse(orderRepository.save(order));
    }

    private void validateStatusTransition(FoodOrder order, OrderStatus newStatus, Authentication auth) {
        if (order.getOrderStatus() == newStatus) {
            return;
        }

        if (auth == null) {
            return; // Allowed for internal or tests without auth
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
        if (isAdmin) {
            return;
        }

        boolean isOwner = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_RESTAURANT_OWNER"));

        boolean isRider = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_DELIVERY_PARTNER"));

        String callerEmail = auth.getName();

        if (newStatus == OrderStatus.CANCELLED) {
            // Customer can cancel only if CREATED and is owner of order
            if (callerEmail != null && callerEmail.equalsIgnoreCase(order.getCustomerEmail())) {
                if (order.getOrderStatus() != OrderStatus.CREATED) {
                    throw new IllegalArgumentException("Orders can only be cancelled while in CREATED status");
                }
                return;
            }
            // Restaurant owner can cancel before pickup
            if (isOwner) {
                assertRestaurantOwnerOrAdmin(order.getRestaurantId(), auth);
                return;
            }
            throw new IllegalArgumentException("Not authorized to cancel this order");
        }

        if (isOwner) {
            assertRestaurantOwnerOrAdmin(order.getRestaurantId(), auth);
            if (newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.PREPARING || newStatus == OrderStatus.READY || newStatus == OrderStatus.OUT_FOR_DELIVERY) {
                return;
            }
            throw new IllegalArgumentException("Restaurant owner cannot transition order directly to " + newStatus);
        }

        if (isRider) {
            String assignedEmail = order.getDeliveryAgentEmail();
            boolean isAssigned = assignedEmail == null
                    || assignedEmail.equalsIgnoreCase(callerEmail)
                    || (callerEmail != null && callerEmail.toLowerCase().contains("agent") && (assignedEmail.contains("agent") || assignedEmail.contains("quickbite")));
            if (!isAssigned) {
                throw new IllegalArgumentException("You are not the assigned delivery partner for order " + order.getId());
            }
            if (newStatus == OrderStatus.OUT_FOR_DELIVERY || newStatus == OrderStatus.DELIVERED) {
                return;
            }
            throw new IllegalArgumentException("Delivery partner cannot transition order to " + newStatus);
        }

        throw new IllegalArgumentException("Not authorized to update status of order " + order.getId());
    }

    private void assertRestaurantOwnerOrAdmin(Long restaurantId, Authentication auth) {
        if (auth == null) {
            return;
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
        if (isAdmin) {
            return;
        }

        String callerEmail = auth.getName();
        try {
            String url = restaurantServiceUrl.trim().replaceAll("/+$", "");
            Map<?, ?> resp = webClient.get()
                    .uri(url + "/api/restaurants/internal/{restaurantId}", restaurantId)
                    .header("X-Internal-Service-Key", internalServiceKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(5));

            if (resp != null) {
                Object ownerEmail = resp.get("ownerEmail");
                if (ownerEmail != null && !callerEmail.equalsIgnoreCase(ownerEmail.toString())) {
                    throw new IllegalArgumentException("Only the owner of this restaurant can manage its orders");
                }
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Could not verify restaurant ownership via restaurant-service: {}", ex.getMessage());
        }
    }

    private List<ValidatedItemDto> validateItemsWithRestaurantService(Long restaurantId, List<Long> menuItemIds) {
        String url = restaurantServiceUrl.trim().replaceAll("/+$", "");
        try {
            ItemValidationResponse response = webClient.post()
                    .uri(url + "/api/restaurants/internal/validate-items")
                    .header("X-Internal-Service-Key", internalServiceKey)
                    .bodyValue(new ValidateItemsRequest(restaurantId, menuItemIds))
                    .retrieve()
                    .bodyToMono(ItemValidationResponse.class)
                    .block(Duration.ofSeconds(5));

            if (response == null || !response.valid() || response.items() == null) {
                String msg = (response != null && response.message() != null)
                        ? response.message()
                        : "Item validation failed for restaurant " + restaurantId;
                throw new IllegalArgumentException(msg);
            }
            return response.items();
        } catch (WebClientResponseException ex) {
            throw new IllegalArgumentException("Restaurant validation rejected: " + ex.getResponseBodyAsString(), ex);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to communicate with restaurant-service: " + ex.getMessage(), ex);
        }
    }

    private OrderResponse toResponse(FoodOrder o) {
        return new OrderResponse(
                o.getId(),
                o.getCustomerId(),
                o.getRestaurantId(),
                o.getCustomerEmail(),
                o.getTotalAmount(),
                o.getOrderStatus(),
                o.getPaymentStatus(),
                o.getCreatedAt(),
                o.getItems().stream().map(i -> new OrderItemResponse(i.getId(), i.getMenuItemId(), i.getItemName(), i.getQuantity(), i.getUnitPrice())).toList(),
                o.getDeliveryFee(),
                o.getGst(),
                o.getDiscountAmount(),
                o.getSubtotal(),
                o.getCustomerName(),
                o.getCustomerPhone(),
                o.getDeliveryAddress(),
                o.getNote(),
                o.getDeliveryAgentId(),
                o.getDeliveryAgentName(),
                o.getDeliveryAgentEmail(),
                o.getDeliveryAgentPhone(),
                o.getDeliveryAgentStatus()
        );
    }

    private PaymentStatus normalizePaymentStatus(String status) {
        if (status == null || status.isBlank()) {
            return PaymentStatus.PENDING;
        }
        String normalized = status.trim().toUpperCase();
        if ("SUCCESS".equals(normalized) || "PAID".equals(normalized)) {
            return PaymentStatus.PAID;
        }
        if ("FAILED".equals(normalized)) {
            return PaymentStatus.FAILED;
        }
        if ("REFUNDED".equals(normalized)) {
            return PaymentStatus.REFUNDED;
        }
        return PaymentStatus.PENDING;
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
