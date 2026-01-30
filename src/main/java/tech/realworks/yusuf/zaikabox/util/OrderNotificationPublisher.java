package tech.realworks.yusuf.zaikabox.util;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tech.realworks.yusuf.zaikabox.entity.OrderEntity;
import tech.realworks.yusuf.zaikabox.io.AdminOrderNotification;

/**
 * Publishes server-side order lifecycle notifications to admin clients over STOMP.
 */
@Component
@RequiredArgsConstructor
public class OrderNotificationPublisher {

    public static final String ADMIN_NEW_ORDER_TOPIC = "/topic/admin/orders";

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Notify admins that a new order has been created.
     */
    public void notifyAdminNewOrder(OrderEntity order) {
        if (order == null) {
            return;
        }

        AdminOrderNotification payload = AdminOrderNotification.builder()
                .orderId(order.getOrderId())
                .totalAmountWithGST(order.getTotalAmountWithGST())
                .paymentMode(order.getPaymentMode())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .orderDate(order.getOrderDate())
                .build();

        messagingTemplate.convertAndSend(ADMIN_NEW_ORDER_TOPIC, payload);
    }

    /**
     * Notify user about order update.
     *
     * @param order The updated order entity
     */
    public void notifyUserOrderUpdate(OrderEntity order) {
        if (order == null) {
            return;
        }
        // Send to topic /topic/orders/{orderId} or similar
        // Since we don't have user-specific queues setup in detail, let's assume client subscribes to /topic/orders/{orderId}
        // The prompt asked for "single WebSocket connection for realtime updates"

        messagingTemplate.convertAndSend("/topic/orders/" + order.getOrderId(), order);
    }
}
