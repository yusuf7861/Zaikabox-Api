package tech.realworks.yusuf.zaikabox.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.realworks.yusuf.zaikabox.entity.CartEntity;
import tech.realworks.yusuf.zaikabox.entity.FoodEntity;
import tech.realworks.yusuf.zaikabox.entity.OrderEntity;
import tech.realworks.yusuf.zaikabox.entity.Status;
import tech.realworks.yusuf.zaikabox.io.OrderItemRequest;
import tech.realworks.yusuf.zaikabox.io.OrderRequest;
import tech.realworks.yusuf.zaikabox.io.OrderResponse;
import tech.realworks.yusuf.zaikabox.repository.CartRepository;
import tech.realworks.yusuf.zaikabox.repository.FoodRepository;
import tech.realworks.yusuf.zaikabox.repository.OrderRepository;
import tech.realworks.yusuf.zaikabox.service.userservice.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BillingServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserService userService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private BillingServiceImpl billingService;

    private static final String USER_ID = "user123";
    private static final String FOOD_ID_1 = "food123";
    private static final String FOOD_ID_2 = "food456";
    private static final String ORDER_ID = "FD12345";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock user service to return a fixed user ID
        when(userService.findByUserId()).thenReturn(USER_ID);

        // Mock order repository to save and return the order
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock food repository to return food entities
        FoodEntity food1 = FoodEntity.builder()
                .id(FOOD_ID_1)
                .name("Paneer Butter Masala")
                .price(220.00)
                .build();

        FoodEntity food2 = FoodEntity.builder()
                .id(FOOD_ID_2)
                .name("Butter Naan")
                .price(40.00)
                .build();

        when(foodRepository.findById(FOOD_ID_1)).thenReturn(Optional.of(food1));
        when(foodRepository.findById(FOOD_ID_2)).thenReturn(Optional.of(food2));
    }

    @Test
    void createOrderFromCartItems() {
        // Arrange
        Map<String, Integer> cartItems = new HashMap<>();
        cartItems.put(FOOD_ID_1, 1); // 1 Paneer Butter Masala
        cartItems.put(FOOD_ID_2, 2); // 2 Butter Naan

        CartEntity cartEntity = new CartEntity(USER_ID, cartItems);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cartEntity));

        OrderRequest orderRequest = OrderRequest.builder()
                .useCart(true)
                .paymentMode("UPI")
                .build();

        // Act
        OrderResponse response = billingService.createOrder(orderRequest);

        // Assert
        assertNotNull(response);
        assertEquals(USER_ID, response.getCustomerId());
        assertEquals("UPI", response.getPaymentMode());
        assertEquals(2, response.getItems().size());

        // Verify subtotal calculation (220 + 2*40 = 300)
        assertEquals(300.0, response.getSubTotal());

        // Verify GST calculation (5% of 300 = 15)
        assertEquals(5.0, response.getGstRate());
        assertEquals(15.0, response.getGstAmount());

        // Verify total with GST (300 + 15 = 315)
        assertEquals(315.0, response.getTotalAmountWithGST());

        // Verify cart was cleared
        verify(cartService, times(1)).clearCart();
    }

    @Test
    void createOrderFromRequestItems() {
        // Arrange
        List<OrderItemRequest> itemRequests = new ArrayList<>();
        itemRequests.add(OrderItemRequest.builder().foodId(FOOD_ID_1).quantity(1).build()); // 1 Paneer Butter Masala
        itemRequests.add(OrderItemRequest.builder().foodId(FOOD_ID_2).quantity(2).build()); // 2 Butter Naan

        OrderRequest orderRequest = OrderRequest.builder()
                .useCart(false)
                .items(itemRequests)
                .paymentMode("UPI")
                .build();

        // Act
        OrderResponse response = billingService.createOrder(orderRequest);

        // Assert
        assertNotNull(response);
        assertEquals(USER_ID, response.getCustomerId());
        assertEquals("UPI", response.getPaymentMode());
        assertEquals(2, response.getItems().size());

        // Verify subtotal calculation (220 + 2*40 = 300)
        assertEquals(300.0, response.getSubTotal());

        // Verify GST calculation (5% of 300 = 15)
        assertEquals(5.0, response.getGstRate());
        assertEquals(15.0, response.getGstAmount());

        // Verify total with GST (300 + 15 = 315)
        assertEquals(315.0, response.getTotalAmountWithGST());

        // Verify cart was not cleared
        verify(cartService, never()).clearCart();
    }

    @Test
    void getOrder() {
        // Arrange
        OrderEntity orderEntity = mock(OrderEntity.class);
        when(orderEntity.getOrderId()).thenReturn(ORDER_ID);
        when(orderEntity.getCustomerId()).thenReturn(USER_ID);
        when(orderEntity.getItems()).thenReturn(Collections.emptyList());

        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(orderEntity));

        // Act
        OrderResponse response = billingService.getOrder(ORDER_ID);

        // Assert
        assertNotNull(response);
        assertEquals(ORDER_ID, response.getOrderId());
        assertEquals(USER_ID, response.getCustomerId());
    }

    @Test
    void getOrders() {
        // Arrange
        OrderEntity orderEntity = mock(OrderEntity.class);
        when(orderEntity.getOrderId()).thenReturn(ORDER_ID);
        when(orderEntity.getCustomerId()).thenReturn(USER_ID);
        when(orderEntity.getItems()).thenReturn(Collections.emptyList());

        when(orderRepository.findByCustomerId(USER_ID)).thenReturn(Collections.singletonList(orderEntity));

        // Act
        List<OrderResponse> responses = billingService.getOrders();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(ORDER_ID, responses.get(0).getOrderId());
        assertEquals(USER_ID, responses.get(0).getCustomerId());
    }

    @Test
    void getOrdersByStatus() {
        // Arrange
        Status status = Status.PENDING;

        OrderEntity orderEntity = mock(OrderEntity.class);
        when(orderEntity.getOrderId()).thenReturn(ORDER_ID);
        when(orderEntity.getCustomerId()).thenReturn(USER_ID);
        when(orderEntity.getItems()).thenReturn(Collections.emptyList());
        when(orderEntity.getStatus()).thenReturn(status);

        when(orderRepository.findByCustomerIdAndStatus(USER_ID, status)).thenReturn(Collections.singletonList(orderEntity));

        // Act
        List<OrderResponse> responses = billingService.getOrdersByStatus(status);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(ORDER_ID, responses.get(0).getOrderId());
        assertEquals(USER_ID, responses.get(0).getCustomerId());
    }

    @Test
    void generateOrderId() {
        // Act
        String orderId = billingService.generateOrderId();

        // Assert
        assertNotNull(orderId);
        assertTrue(orderId.startsWith("FD"));
        assertEquals(10, orderId.length());
    }

    @Test
    void generateTextBill() {
        // Arrange
        OrderEntity orderEntity = mock(OrderEntity.class);
        when(orderEntity.getOrderId()).thenReturn(ORDER_ID);
        when(orderEntity.getCustomerId()).thenReturn(USER_ID);
        when(orderEntity.getPaymentMode()).thenReturn("CARD");
        when(orderEntity.getStatus()).thenReturn(Status.valueOf(Status.DELIVERED.name()));
        when(orderEntity.getSubTotal()).thenReturn(300.0);
        when(orderEntity.getGstRate()).thenReturn(5.0);
        when(orderEntity.getGstAmount()).thenReturn(15.0);
        when(orderEntity.getTotalAmountWithGST()).thenReturn(315.0);

        // Mock order items
        List<tech.realworks.yusuf.zaikabox.entity.OrderItemEntity> orderItems = new ArrayList<>();
        tech.realworks.yusuf.zaikabox.entity.OrderItemEntity item1 = mock(tech.realworks.yusuf.zaikabox.entity.OrderItemEntity.class);
        when(item1.getName()).thenReturn("Paneer Butter Masala");
        when(item1.getQuantity()).thenReturn(1);
        when(item1.getUnitPrice()).thenReturn(220.0);
        when(item1.getTotal()).thenReturn(220.0);

        tech.realworks.yusuf.zaikabox.entity.OrderItemEntity item2 = mock(tech.realworks.yusuf.zaikabox.entity.OrderItemEntity.class);
        when(item2.getName()).thenReturn("Butter Naan");
        when(item2.getQuantity()).thenReturn(2);
        when(item2.getUnitPrice()).thenReturn(40.0);
        when(item2.getTotal()).thenReturn(80.0);

        orderItems.add(item1);
        orderItems.add(item2);
        when(orderEntity.getItems()).thenReturn(orderItems);

        // Mock order date
        java.time.LocalDateTime orderDate = java.time.LocalDateTime.of(2023, 6, 15, 14, 30, 45);
        when(orderEntity.getOrderDate()).thenReturn(orderDate);

        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(orderEntity));

        // Act
        String textBill = billingService.generateTextBill(ORDER_ID);

        // Assert
        assertNotNull(textBill);
        assertTrue(textBill.contains("ZaikaBox - Order Bill"));
        assertTrue(textBill.contains("Order ID: " + ORDER_ID));
        assertTrue(textBill.contains("Order Date: 15-06-2023 14:30:45"));
        assertTrue(textBill.contains("Payment Mode: CARD"));
        assertTrue(textBill.contains("Status: DELIVERED"));

        // Check item details
        assertTrue(textBill.contains("Paneer Butter Masala"));
        assertTrue(textBill.contains("Butter Naan"));

        // Check totals
        assertTrue(textBill.contains("Subtotal:"));
        assertTrue(textBill.contains("300.00"));
        assertTrue(textBill.contains("GST (5.0%):"));
        assertTrue(textBill.contains("15.00"));
        assertTrue(textBill.contains("Total:"));
        assertTrue(textBill.contains("315.00"));

        // Check footer
        assertTrue(textBill.contains("Thank you for your order!"));
    }
}
