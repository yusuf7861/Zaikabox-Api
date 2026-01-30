                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            package tech.realworks.yusuf.zaikabox.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payment_requests")
public class PaymentRequestEntity {
    @Id
    private String id;

    // User info
    private String customerId; // User ID
    private String orderId; // Custom Order ID (e.g. FD12345)

    // Razorpay info
    private String razorpayOrderId;
    private String currency;

    // Order snapshot
    private List<OrderItemEntity> items;
    private double subTotal;
    private double gstRate;
    private double gstAmount;
    private double totalAmountWithGST;
    private BillingDetails billingDetails;
    private String paymentMode;

    // Internal state
    private boolean useCart; // To clear cart later
    private String status; // PENDING, COMPLETED, FAILED
    private LocalDateTime createdAt;
}
