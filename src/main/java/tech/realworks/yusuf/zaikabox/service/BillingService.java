package tech.realworks.yusuf.zaikabox.service;

import com.razorpay.RazorpayException;
import tech.realworks.yusuf.zaikabox.entity.Status;
import tech.realworks.yusuf.zaikabox.io.OrderRequest;
import tech.realworks.yusuf.zaikabox.io.OrderResponse;

import java.util.List;

/**
 * Service interface for billing operations.
 */
public interface BillingService {

    /**
     * Create a new order
     * @param orderRequest The order request containing order details
     * @return The created order response
     */
    OrderResponse createOrder(OrderRequest orderRequest) throws RazorpayException;

    /**
     * Get an order by its ID
     * @param orderId The order ID
     * @return The order response
     */
    OrderResponse getOrder(String orderId);

    /**
     * Get all orders for the current user
     * @return List of order responses
     */
    List<OrderResponse> getOrders();

    /**
     * Get all orders for the current user with a specific status
     * @param status The order status
     * @return List of order responses
     */
    List<OrderResponse> getOrdersByStatus(Status status);

    /**
     * Generate a unique order ID
     * @return A unique order ID (e.g., FD12345)
     */
    String generateOrderId();

    /**
     * Generate a PDF bill for an order
     * @param orderId The order ID
     * @return Byte array containing the PDF data
     */
    byte[] generatePdfBill(String orderId);

    /**
     * Generate a text representation of the bill for an order
     * @param orderId The order ID
     * @return String containing the text representation of the bill
     */
    String generateTextBill(String orderId);
}
