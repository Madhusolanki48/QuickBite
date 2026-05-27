package com.quickbite.orderservice.service;

import com.quickbite.orderservice.client.DeliveryServiceClient;
import com.quickbite.orderservice.config.PricingProperties;
import com.quickbite.orderservice.dto.AssignDeliveryRequest;
import com.quickbite.orderservice.dto.CreateOrderRequest;
import com.quickbite.orderservice.dto.OrderItemRequest;
import com.quickbite.orderservice.dto.OrderResponse;
import com.quickbite.orderservice.dto.internal.ItemValidationResponse;
import com.quickbite.orderservice.dto.internal.ValidatedItemDto;
import com.quickbite.orderservice.model.DeliveryStatus;
import com.quickbite.orderservice.model.FoodOrder;
import com.quickbite.orderservice.model.OrderStatus;
import com.quickbite.orderservice.model.PaymentStatus;
import com.quickbite.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MailService mailService;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private DeliveryServiceClient deliveryServiceClient;

    private OrderService orderService;
    private PricingProperties pricingProperties;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        pricingProperties = new PricingProperties(49.0, 0.05);
        orderService = new OrderService(
                orderRepository,
                mailService,
                webClientBuilder,
                pricingProperties,
                deliveryServiceClient,
                "http://restaurant-service",
                "quickbite-internal-secret-token"
        );
    }

    private void mockWebClient(ItemValidationResponse response) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ItemValidationResponse.class)).thenReturn(Mono.just(response));
    }

    @Test
    void create_calculatesSubtotalGstAndTotalAuthoritatively() {
        ValidatedItemDto item1 = new ValidatedItemDto(
                11L, 1L, "Burger Palace", 1L, "Burgers", "Classic Cheeseburger", 199.0, 10.0, 179.1, true, true);
        ValidatedItemDto item2 = new ValidatedItemDto(
                12L, 1L, "Burger Palace", 1L, "Burgers", "French Fries", 99.0, null, 99.0, true, true);
        ItemValidationResponse validation = new ItemValidationResponse(
                true, 1L, "Burger Palace", List.of(item1, item2), null);
        mockWebClient(validation);

        when(orderRepository.save(any(FoodOrder.class))).thenAnswer(invocation -> {
            FoodOrder order = invocation.getArgument(0);
            order.setId(10L);
            return order;
        });

        CreateOrderRequest request = new CreateOrderRequest(
                1L,
                1L,
                "customer@example.com",
                List.of(
                        new OrderItemRequest(11L, "Classic Cheeseburger", 2, 10.0), // client spoofed price
                        new OrderItemRequest(12L, "French Fries", 1, 5.0)),
                null,
                null,
                null,
                null,
                null,
                999.0, // client spoofed discount
                null,
                null,
                null,
                null,
                null);

        OrderResponse response = orderService.create(request);

        // Subtotal = 2 * 179.1 + 1 * 99.0 = 358.2 + 99.0 = 457.2
        assertEquals(457.2, response.subtotal());
        assertEquals(0.0, response.discountAmount());
        assertEquals(49.0, response.deliveryFee());
        // GST = round(457.2 * 0.05) = 22.86
        assertEquals(22.86, response.gst());
        // Total = 457.2 + 49.0 + 22.86 = 529.06
        assertEquals(529.06, response.totalAmount());
        assertEquals(OrderStatus.CREATED, response.orderStatus());
        assertEquals(DeliveryStatus.UNASSIGNED, response.deliveryAgentStatus());
    }

    @Test
    void create_appliesFood10PromoCodeAccurately() {
        ValidatedItemDto item = new ValidatedItemDto(
                11L, 1L, "Burger Palace", 1L, "Burgers", "Classic Cheeseburger", 200.0, null, 200.0, true, true);
        ItemValidationResponse validation = new ItemValidationResponse(
                true, 1L, "Burger Palace", List.of(item), null);
        mockWebClient(validation);

        when(orderRepository.save(any(FoodOrder.class))).thenAnswer(invocation -> {
            FoodOrder order = invocation.getArgument(0);
            order.setId(11L);
            return order;
        });

        CreateOrderRequest request = new CreateOrderRequest(
                1L,
                1L,
                "customer@example.com",
                List.of(new OrderItemRequest(11L, "Classic Cheeseburger", 1, 200.0)),
                "FOOD10",
                100.0, // spoofed discount
                null,
                null,
                null,
                null,
                null);

        OrderResponse response = orderService.create(request);

        // Subtotal = 200.0, discount = 50.0, deliveryFee = 49.0, GST = round(200.0 * 0.05) = 10.0
        // Total = (200 - 50) + 49 + 10 = 209.0
        assertEquals(200.0, response.subtotal());
        assertEquals(50.0, response.discountAmount());
        assertEquals(209.0, response.totalAmount());
    }

    @Test
    void create_rejectsWhenMenuItemIsUnavailable() {
        ValidatedItemDto item = new ValidatedItemDto(
                11L, 1L, "Burger Palace", 1L, "Burgers", "Classic Cheeseburger", 200.0, null, 200.0, false, true);
        ItemValidationResponse validation = new ItemValidationResponse(
                true, 1L, "Burger Palace", List.of(item), null);
        mockWebClient(validation);

        CreateOrderRequest request = new CreateOrderRequest(
                1L,
                1L,
                "customer@example.com",
                List.of(new OrderItemRequest(11L, "Classic Cheeseburger", 1, 200.0)),
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThrows(IllegalArgumentException.class, () -> orderService.create(request));
    }

    @Test
    void create_marksRazorpaySuccessAsPaid() {
        ValidatedItemDto item = new ValidatedItemDto(
                21L, 2L, "Spice Garden", 5L, "Main Course", "Water", 20.0, null, 20.0, true, true);
        ItemValidationResponse validation = new ItemValidationResponse(
                true, 2L, "Spice Garden", List.of(item), null);
        mockWebClient(validation);

        when(orderRepository.save(any(FoodOrder.class))).thenAnswer(invocation -> {
            FoodOrder order = invocation.getArgument(0);
            order.setId(13L);
            return order;
        });

        OrderResponse response = orderService.create(new CreateOrderRequest(
                1L,
                2L,
                "sonam@example.com",
                List.of(new OrderItemRequest(21L, "Water", 1, 20.0)),
                null,
                null,
                "UPI",
                "SUCCESS",
                "pay_test",
                "order_test",
                "sig_test"));

        assertThat(response.id()).isEqualTo(13L);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void assignDelivery_validatesRiderAndPersistsAuthoritativeDetails() {
        FoodOrder order = FoodOrder.builder()
                .id(99L)
                .restaurantId(1L)
                .customerEmail("customer@example.com")
                .deliveryAddress("Connaught Place, Delhi")
                .orderStatus(OrderStatus.CONFIRMED)
                .deliveryAgentStatus(DeliveryStatus.UNASSIGNED)
                .build();

        when(orderRepository.findById(99L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(FoodOrder.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryServiceClient.InternalRiderDto rider = new DeliveryServiceClient.InternalRiderDto(
                5L, "Rahul Kumar", "rahul.kumar@quickbite.dev", "+91 98765 43210", true, true
        );
        when(deliveryServiceClient.validateAndGetRider(5L)).thenReturn(rider);

        OrderResponse response = orderService.assignDelivery(99L, new AssignDeliveryRequest(5L), null);

        assertThat(response.deliveryAgentId()).isEqualTo(5L);
        assertThat(response.deliveryAgentName()).isEqualTo("Rahul Kumar");
        assertThat(response.deliveryAgentEmail()).isEqualTo("rahul.kumar@quickbite.dev");
        assertThat(response.deliveryAgentPhone()).isEqualTo("+91 98765 43210");
        assertThat(response.deliveryAgentStatus()).isEqualTo(DeliveryStatus.ASSIGNED);
        // Order status remains CONFIRMED (not changed to PICKED_UP or OUT_FOR_DELIVERY)
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void updateStatus_decouplesDeliveryStatusAndOrderStatus() {
        FoodOrder order = FoodOrder.builder()
                .id(100L)
                .restaurantId(1L)
                .customerEmail("customer@example.com")
                .orderStatus(OrderStatus.READY)
                .deliveryAgentStatus(DeliveryStatus.ASSIGNED)
                .build();

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(FoodOrder.class))).thenAnswer(i -> i.getArgument(0));

        // Rider pickup: order status OUT_FOR_DELIVERY, delivery status PICKED_UP
        OrderResponse outForDelivery = orderService.updateStatus(100L, OrderStatus.OUT_FOR_DELIVERY, null);
        assertThat(outForDelivery.orderStatus()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);
        assertThat(outForDelivery.deliveryAgentStatus()).isEqualTo(DeliveryStatus.PICKED_UP);

        // Rider complete: order status DELIVERED, delivery status DELIVERED
        OrderResponse delivered = orderService.updateStatus(100L, OrderStatus.DELIVERED, null);
        assertThat(delivered.orderStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(delivered.deliveryAgentStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }
}
