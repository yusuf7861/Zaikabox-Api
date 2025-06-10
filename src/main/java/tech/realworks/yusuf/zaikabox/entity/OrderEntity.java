package tech.realworks.yusuf.zaikabox.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents an order entity in the ZaikaBox application.
 * This class is used to store order-related information in the MongoDB database.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "orders")
public class OrderEntity {
    @Id
    private String id;
    private String orderId; // Custom order ID (e.g., FD12345)
    private String customerId; // Reference to the user who placed the order
    private List<OrderItemEntity> items; // List of items in the order
    private double subTotal; // Total before tax
    private double gstRate; // GST rate in percentage
    private double gstAmount; // GST amount
    private double totalAmountWithGST; // Total amount including GST
    private String paymentMode; // Payment mode (e.g., UPI, CARD, COD)
    private LocalDateTime orderDate; // Date and time when the order was placed
    private Status status; // Order status (e.g., PENDING, CONFIRMED, DELIVERED)
}