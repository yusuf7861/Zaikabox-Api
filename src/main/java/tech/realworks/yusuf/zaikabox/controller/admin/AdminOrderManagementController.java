package tech.realworks.yusuf.zaikabox.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.realworks.yusuf.zaikabox.entity.Status;
import tech.realworks.yusuf.zaikabox.io.AdminOrderDTO;
import tech.realworks.yusuf.zaikabox.io.ErrorsResponse;
import tech.realworks.yusuf.zaikabox.service.AdminOrderManagementService;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Controller for admin order management operations.
 * Allows admins to view recent orders and update their status.
 * Status updates are broadcasted to customers in real-time via WebSocket.
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Order Management", description = "Admin APIs for order management and tracking")
public class AdminOrderManagementController {

    private final AdminOrderManagementService adminOrderManagementService;

    @Operation(summary = "Get recent orders", description = "Retrieves the last 50 orders sorted by most recent first")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AdminOrderDTO.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
    })
    @GetMapping("/recent")
    public ResponseEntity<List<AdminOrderDTO>> getRecentOrders() {
        List<AdminOrderDTO> orders = adminOrderManagementService.getRecentOrders();
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get orders by status", description = "Retrieves all orders with a specific status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AdminOrderDTO>> getOrdersByStatus(@PathVariable Status status) {
        List<AdminOrderDTO> orders = adminOrderManagementService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Update order status",
            description = "Update order status and notify customer in real-time via WebSocket. " +
                    "Status changes: PENDING → PROCESSING → DELIVERED or CANCELLED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status updated successfully",
                    content = @Content(schema = @Schema(implementation = AdminOrderDTO.class))),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition")
    })
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam Status status) {
        try {
            AdminOrderDTO updatedOrder = adminOrderManagementService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorsResponse("Order not found: " + orderId, HttpStatus.NOT_FOUND));
        }
    }

    @Operation(summary = "Get order count by status", description = "Get the count of orders in each status")
    @ApiResponse(responseCode = "200", description = "Order counts retrieved successfully")
    @GetMapping("/stats/count")
    public ResponseEntity<?> getOrderCountByStatus() {
        try {
            class OrderStats {
                public final long pending;
                public final long processing;
                public final long delivered;
                public final long cancelled;

                OrderStats() {
                    this.pending = adminOrderManagementService.getOrdersByStatus(Status.PENDING).size();
                    this.processing = adminOrderManagementService.getOrdersByStatus(Status.PROCESSING).size();
                    this.delivered = adminOrderManagementService.getOrdersByStatus(Status.DELIVERED).size();
                    this.cancelled = adminOrderManagementService.getOrdersByStatus(Status.CANCELLED).size();
                }
            }
            return ResponseEntity.ok(new OrderStats());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorsResponse("Failed to fetch order stats", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}
