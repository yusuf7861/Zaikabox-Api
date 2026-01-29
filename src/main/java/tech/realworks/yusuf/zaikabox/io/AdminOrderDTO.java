package tech.realworks.yusuf.zaikabox.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.realworks.yusuf.zaikabox.entity.OrderItemEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for recent orders displayed to admin dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderDTO {
    private String orderId;
    private List<OrderItemEntity> items;
    private double subTotal;
    private double gstAmount;
    private double totalAmountWithGST;
    private String paymentMode;
    private String status;
    private LocalDateTime orderDate;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String locality;
}
