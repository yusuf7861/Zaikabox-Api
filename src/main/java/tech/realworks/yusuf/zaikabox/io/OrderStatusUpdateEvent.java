package tech.realworks.yusuf.zaikabox.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for order status update events sent to customers via WebSocket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateEvent {
    private String orderId;
    private String previousStatus;
    private String newStatus;
    private Long updatedAt;
    private String updatedBy; // Admin user ID
}
