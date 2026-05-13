package com.quickbite.orderservice.service;

import com.quickbite.orderservice.dto.CreateOrderRequest;
import com.quickbite.orderservice.dto.OrderItemRequest;
import com.quickbite.orderservice.model.FoodOrder;
import com.quickbite.orderservice.model.OrderStatus;
import com.quickbite.orderservice.model.PaymentStatus;
import com.quickbite.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private MailService mailService;
    private OrderService orderService;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        orderService = new OrderService(orderRepository, mailService);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void createCalculatesTotalAndDefaultsStatuses() {
        when(orderRepository.save(any(FoodOrder.class))).thenAnswer(invocation -> {
            FoodOrder order = invocation.getArgument(0);
            order.setId(11L);
            return order;
        });

        var response = orderService.create(new CreateOrderRequest(
                1L,
                2L,
                "sonam@example.com",
                List.of(
                        new OrderItemRequest(21L, "Chhole Bhature", 2, 289.0),
                        new OrderItemRequest(22L, "Lassi", 1, 129.0)),
                "SAVE10",
                0.0));

        assertThat(response.id()).isEqualTo(11L);
        assertThat(response.totalAmount()).isEqualTo(707.0);
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void createClampsNegativeDiscountAndNeverReturnsNegativeTotal() {
        when(orderRepository.save(any(FoodOrder.class))).thenAnswer(invocation -> {
            FoodOrder order = invocation.getArgument(0);
            order.setId(12L);
            return order;
        });

        var response = orderService.create(new CreateOrderRequest(
                1L,
                2L,
                "sonam@example.com",
                List.of(new OrderItemRequest(21L, "Water", 1, 20.0)),
                "SAVE999",
                0.0));

        assertThat(response.id()).isEqualTo(12L);
        assertThat(response.totalAmount()).isEqualTo(20.0);
    }
}
