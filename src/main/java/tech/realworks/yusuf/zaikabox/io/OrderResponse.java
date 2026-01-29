package tech.realworks.yusuf.zaikabox.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for order response.
 * Used to return order details to clients.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private String orderId; // Custom order ID (e.g., FD12345)
    private String customerId; // ID of the customer who placed the order
    private List<OrderItemResponse> items; // List of items in the order
    private double subTotal; // Total before tax
    private double gstRate; // GST rate in percentage
    private double gstAmount; // GST amount
    private double totalAmountWithGST; // Total amount including GST
    private String paymentMode; // Payment mode (e.g., UPI, CARD, COD)
    private LocalDateTime orderDate; // Date and time when the order was placed
    private String status; // Order status (e.g., PENDING, CONFIRMED, DELIVERED)
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String paymentStatus;
    private LocalDateTime paymentDate;

    // Billing details
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String zip;
    private String locality;
    private String landmark;
    private String country;
    private String state;
}
