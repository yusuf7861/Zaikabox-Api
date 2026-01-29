package tech.realworks.yusuf.zaikabox.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lightweight payload sent to admins over WebSocket/STOMP when a new order is placed.
 * Intentionally excludes PII (e.g., full billing details).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderNotification {
    private String orderId;
    private double totalAmountWithGST;
    private String paymentMode;
    private String status;
    private LocalDateTime orderDate;
}
