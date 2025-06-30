package tech.realworks.yusuf.zaikabox.controller;

import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.realworks.yusuf.zaikabox.entity.Status;
import tech.realworks.yusuf.zaikabox.io.ErrorsResponse;
import tech.realworks.yusuf.zaikabox.io.OrderRequest;
import tech.realworks.yusuf.zaikabox.io.OrderResponse;
import tech.realworks.yusuf.zaikabox.service.BillingService;
import tech.realworks.yusuf.zaikabox.service.OrderService;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controller for handling billing and order-related operations
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;
    private final OrderService orderService;

    /**
     * Create a new order
     * @param orderRequest The order request containing order details
     * @return ResponseEntity containing the created order response
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) throws RazorpayException {
        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.createOrder(orderRequest));
    }

    /**
     * Get an order by its ID
     * @param orderId The order ID
     * @return ResponseEntity containing the order response
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(billingService.getOrder(orderId));
    }

    /**
     * Get all orders for the current user
     * @return ResponseEntity containing a list of order responses
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders() {
        return ResponseEntity.ok(billingService.getOrders());
    }

    /**
     * Get all orders for the current user with a specific status
     * @param status The order status
     * @return ResponseEntity containing a list of order responses
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable Status status) {
        return ResponseEntity.ok(billingService.getOrdersByStatus(status));
    }

    /**
     * Generate a PDF bill for an order
     * @param orderId The order ID
     * @return ResponseEntity containing the PDF bill
     */
    @GetMapping("/{orderId}/bill/pdf")
    public ResponseEntity<byte[]> generatePdfBill(@PathVariable String orderId) {
        byte[] pdfContent = billingService.generatePdfBill(orderId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "order-" + orderId + "-bill.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

    /**
     * Generate a text bill for an order
     * @param orderId The order ID
     * @return ResponseEntity containing the text bill
     */
    @GetMapping("/{orderId}/bill/text")
    public ResponseEntity<byte[]> generateTextBill(@PathVariable String orderId) {
        String textContent = billingService.generateTextBill(orderId);
        byte[] textBytes = textContent.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("filename", "order-" + orderId + "-bill.txt");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(textBytes, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable String orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.ok("Order deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(new ErrorsResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}")
    public void updateOrderStatus(@PathVariable String orderId, Status status) {
        orderService.changeStatusOfOrder(orderId, status);
    }
}
