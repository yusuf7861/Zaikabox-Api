package tech.realworks.yusuf.zaikabox.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tech.realworks.yusuf.zaikabox.entity.OrderEntity;
import tech.realworks.yusuf.zaikabox.entity.Status;
import tech.realworks.yusuf.zaikabox.io.AdminOrderDTO;
import tech.realworks.yusuf.zaikabox.io.OrderStatusUpdateEvent;
import tech.realworks.yusuf.zaikabox.repository.OrderRepository;
import tech.realworks.yusuf.zaikabox.service.userservice.UserService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Service for admin order management operations.
 * Handles fetching recent orders and updating order status.
 * Broadcasts status changes to customers via WebSocket.
 */
@Service
@RequiredArgsConstructor
public class AdminOrderManagementService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public static final String CUSTOMER_ORDER_STATUS_TOPIC = "/topic/orders";

    /**
     * Get all recent orders (last 50) sorted by date descending
     */
    public List<AdminOrderDTO> getRecentOrders() {
        List<OrderEntity> orders = orderRepository.findAll().stream()
                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                .limit(50)
                .toList();

        return orders.stream()
                .map(this::convertToAdminDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get orders filtered by status
     */
    public List<AdminOrderDTO> getOrdersByStatus(Status status) {
        List<OrderEntity> orders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == status)
                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                .toList();

        return orders.stream()
                .map(this::convertToAdminDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update order status and broadcast to customer
     * @param orderId The order ID to update
     * @param newStatus The new status
     * @return Updated order details
     */
    public AdminOrderDTO updateOrderStatus(String orderId, Status newStatus) {
        OrderEntity order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));

        String previousStatus = order.getStatus().name();
        order.setStatus(newStatus);
        order = orderRepository.save(order);

        // Get current admin user ID
        String adminUserId = userService.findByUserId();

        // Broadcast status update to customer via WebSocket
        broadcastStatusUpdate(orderId, previousStatus, newStatus.name(), adminUserId);

        return convertToAdminDTO(order);
    }

    /**
     * Broadcast order status update to customer via WebSocket
     * Customer subscribes to /topic/orders/{orderId} to receive updates
     */
    private void broadcastStatusUpdate(String orderId, String previousStatus, String newStatus, String adminUserId) {
        OrderStatusUpdateEvent event = OrderStatusUpdateEvent.builder()
                .orderId(orderId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .updatedAt(System.currentTimeMillis())
                .updatedBy(adminUserId)
                .build();

        // Send to both:
        // 1. Order-specific topic for customer tracking
        messagingTemplate.convertAndSend("/topic/orders/" + orderId, event);

        // 2. General topic for all admins monitoring the order
        messagingTemplate.convertAndSend("/topic/admin/orders/status-update", event);
    }

    /**
     * Convert OrderEntity to AdminOrderDTO
     */
    private AdminOrderDTO convertToAdminDTO(OrderEntity order) {
        AdminOrderDTO dto = AdminOrderDTO.builder()
                .orderId(order.getOrderId())
                .items(order.getItems())
                .subTotal(order.getSubTotal())
                .gstAmount(order.getGstAmount())
                .totalAmountWithGST(order.getTotalAmountWithGST())
                .paymentMode(order.getPaymentMode())
                .status(order.getStatus().name())
                .orderDate(order.getOrderDate())
                .build();

        // Add billing details if available
        if (order.getBillingDetails() != null) {
            dto.setFirstName(order.getBillingDetails().getFirstName());
            dto.setLastName(order.getBillingDetails().getLastName());
            dto.setEmail(order.getBillingDetails().getEmail());
            dto.setAddress(order.getBillingDetails().getAddress());
            dto.setLocality(order.getBillingDetails().getLocality());
        }

        return dto;
    }
}
