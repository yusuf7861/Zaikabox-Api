package tech.realworks.yusuf.zaikabox.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tech.realworks.yusuf.zaikabox.entity.Status;
import tech.realworks.yusuf.zaikabox.io.OrderItemRequest;
import tech.realworks.yusuf.zaikabox.io.OrderItemResponse;
import tech.realworks.yusuf.zaikabox.io.OrderRequest;
import tech.realworks.yusuf.zaikabox.io.OrderResponse;
import tech.realworks.yusuf.zaikabox.service.BillingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BillingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BillingService billingService;

    @InjectMocks
    private BillingController billingController;

    private ObjectMapper objectMapper;

    private static final String USER_ID = "user123";
    private static final String ORDER_ID = "FD12345";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(billingController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization
    }

    @Test
    void createOrder() throws Exception {
        // Arrange
        List<OrderItemRequest> itemRequests = new ArrayList<>();
        itemRequests.add(OrderItemRequest.builder().foodId("food123").quantity(1).build());
        itemRequests.add(OrderItemRequest.builder().foodId("food456").quantity(2).build());

        OrderRequest orderRequest = OrderRequest.builder()
                .useCart(false)
                .items(itemRequests)
                .paymentMode("UPI")
                .build();

        List<OrderItemResponse> itemResponses = new ArrayList<>();
        itemResponses.add(OrderItemResponse.builder()
                .name("Paneer Butter Masala")
                .quantity(1)
                .unitPrice(220.00)
                .total(220.00)
                .build());
        itemResponses.add(OrderItemResponse.builder()
                .name("Butter Naan")
                .quantity(2)
                .unitPrice(40.00)
                .total(80.00)
                .build());

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(ORDER_ID)
                .customerId(USER_ID)
                .items(itemResponses)
                .subTotal(300.00)
                .gstRate(5.0)
                .gstAmount(15.00)
                .totalAmountWithGST(315.00)
                .paymentMode("UPI")
                .orderDate(LocalDateTime.now())
                .status("PENDING")
                .build();

        when(billingService.createOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId", is(ORDER_ID)))
                .andExpect(jsonPath("$.customerId", is(USER_ID)))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].name", is("Paneer Butter Masala")))
                .andExpect(jsonPath("$.items[0].quantity", is(1)))
                .andExpect(jsonPath("$.items[0].unitPrice", is(220.00)))
                .andExpect(jsonPath("$.items[0].total", is(220.00)))
                .andExpect(jsonPath("$.items[1].name", is("Butter Naan")))
                .andExpect(jsonPath("$.items[1].quantity", is(2)))
                .andExpect(jsonPath("$.items[1].unitPrice", is(40.00)))
                .andExpect(jsonPath("$.items[1].total", is(80.00)))
                .andExpect(jsonPath("$.subTotal", is(300.00)))
                .andExpect(jsonPath("$.gstRate", is(5.0)))
                .andExpect(jsonPath("$.gstAmount", is(15.00)))
                .andExpect(jsonPath("$.totalAmountWithGST", is(315.00)))
                .andExpect(jsonPath("$.paymentMode", is("UPI")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void getOrder() throws Exception {
        // Arrange
        List<OrderItemResponse> itemResponses = Collections.singletonList(
                OrderItemResponse.builder()
                        .name("Paneer Butter Masala")
                        .quantity(1)
                        .unitPrice(220.00)
                        .total(220.00)
                        .build()
        );

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(ORDER_ID)
                .customerId(USER_ID)
                .items(itemResponses)
                .subTotal(220.00)
                .gstRate(5.0)
                .gstAmount(11.00)
                .totalAmountWithGST(231.00)
                .paymentMode("UPI")
                .orderDate(LocalDateTime.now())
                .status("PENDING")
                .build();

        when(billingService.getOrder(ORDER_ID)).thenReturn(orderResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/{orderId}", ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(ORDER_ID)))
                .andExpect(jsonPath("$.customerId", is(USER_ID)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.subTotal", is(220.00)))
                .andExpect(jsonPath("$.gstRate", is(5.0)))
                .andExpect(jsonPath("$.gstAmount", is(11.00)))
                .andExpect(jsonPath("$.totalAmountWithGST", is(231.00)))
                .andExpect(jsonPath("$.paymentMode", is("UPI")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void getOrders() throws Exception {
        // Arrange
        List<OrderItemResponse> itemResponses = Collections.singletonList(
                OrderItemResponse.builder()
                        .name("Paneer Butter Masala")
                        .quantity(1)
                        .unitPrice(220.00)
                        .total(220.00)
                        .build()
        );

        OrderResponse orderResponse1 = OrderResponse.builder()
                .orderId(ORDER_ID)
                .customerId(USER_ID)
                .items(itemResponses)
                .subTotal(220.00)
                .gstRate(5.0)
                .gstAmount(11.00)
                .totalAmountWithGST(231.00)
                .paymentMode("UPI")
                .orderDate(LocalDateTime.now())
                .status("PENDING")
                .build();

        OrderResponse orderResponse2 = OrderResponse.builder()
                .orderId("FD67890")
                .customerId(USER_ID)
                .items(itemResponses)
                .subTotal(220.00)
                .gstRate(5.0)
                .gstAmount(11.00)
                .totalAmountWithGST(231.00)
                .paymentMode("CARD")
                .orderDate(LocalDateTime.now())
                .status("DELIVERED")
                .build();

        when(billingService.getOrders()).thenReturn(Arrays.asList(orderResponse1, orderResponse2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].orderId", is(ORDER_ID)))
                .andExpect(jsonPath("$[0].customerId", is(USER_ID)))
                .andExpect(jsonPath("$[0].paymentMode", is("UPI")))
                .andExpect(jsonPath("$[0].status", is("PENDING")))
                .andExpect(jsonPath("$[1].orderId", is("FD67890")))
                .andExpect(jsonPath("$[1].customerId", is(USER_ID)))
                .andExpect(jsonPath("$[1].paymentMode", is("CARD")))
                .andExpect(jsonPath("$[1].status", is("DELIVERED")));
    }

    @Test
    void getOrdersByStatus() throws Exception {
        // Arrange
        Status status = Status.PENDING;

        List<OrderItemResponse> itemResponses = Collections.singletonList(
                OrderItemResponse.builder()
                        .name("Paneer Butter Masala")
                        .quantity(1)
                        .unitPrice(220.00)
                        .total(220.00)
                        .build()
        );

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(ORDER_ID)
                .customerId(USER_ID)
                .items(itemResponses)
                .subTotal(220.00)
                .gstRate(5.0)
                .gstAmount(11.00)
                .totalAmountWithGST(231.00)
                .paymentMode("UPI")
                .orderDate(LocalDateTime.now())
                .status(String.valueOf(status))
                .build();

        when(billingService.getOrdersByStatus(status)).thenReturn(Collections.singletonList(orderResponse));

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/status/{status}", status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId", is(ORDER_ID)))
                .andExpect(jsonPath("$[0].customerId", is(USER_ID)))
                .andExpect(jsonPath("$[0].status", is(status)));
    }

    @Test
    void generateTextBill() throws Exception {
        // Arrange
        String sampleTextBill = "===========================================\n" +
                "            ZaikaBox - Order Bill          \n" +
                "===========================================\n\n" +
                "Order ID: " + ORDER_ID + "\n" +
                "Order Date: 15-06-2023 14:30:45\n" +
                "Payment Mode: CARD\n" +
                "Status: COMPLETED\n\n" +
                "No.   Item                           Qty      Unit Price   Total       \n" +
                "-------------------------------------------------------------------\n" +
                "1     Paneer Butter Masala           1        $220.00      $220.00     \n" +
                "2     Butter Naan                    2        $40.00       $80.00      \n" +
                "-------------------------------------------------------------------\n\n" +
                "Subtotal:                                      $300.00\n" +
                "GST (5.0%):                                    $15.00\n" +
                "Total:                                         $315.00\n\n" +
                "===========================================\n" +
                "          Thank you for your order!        \n" +
                "===========================================\n";

        when(billingService.generateTextBill(ORDER_ID)).thenReturn(sampleTextBill);

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/{orderId}/bill/text", ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Content-Disposition", containsString("filename=order-" + ORDER_ID + "-bill.txt")))
                .andExpect(content().string(sampleTextBill));
    }
}
