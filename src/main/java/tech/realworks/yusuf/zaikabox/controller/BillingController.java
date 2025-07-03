package tech.realworks.yusuf.zaikabox.controller;

import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
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
import tech.realworks.yusuf.zaikabox.io.RazorpayPaymentVerificationDTO;
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
@Tag(name = "Billing & Orders", description = "APIs for billing and order management")
public class BillingController {

    private final BillingService billingService;
    private final OrderService orderService;

    @Value("${razorpay.secret.key}")
    private String RAZORPAY_SECRET;

    /**
     * Create a new order
     * @param orderRequest The order request containing order details
     * @return ResponseEntity containing the created order response
     */
    @Operation(summary = "Create a new order", description = "Creates a new order and returns the order details.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid order request", content = @Content(schema = @Schema(implementation = ErrorsResponse.class)))
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) throws RazorpayException {
        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.createOrder(orderRequest));
    }

    /**
     * Verifies payment requests.
     * @param dto the payment verification data transfer object
     * @return ResponseEntity with a success message if verified, or an error message otherwise
     */
    @Operation(summary = "Verify payment", description = "Verifies a Razorpay payment signature.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payment signature"),
        @ApiResponse(responseCode = "500", description = "Verification failed")
    })
    @PostMapping("/verify-payment")
    public ResponseEntity<String> verifyPayment(@RequestBody RazorpayPaymentVerificationDTO dto) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", dto.getRazorpayOrderId());
            options.put("razorpay_payment_id", dto.getRazorpayPaymentId());
            options.put("razorpay_signature", dto.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, RAZORPAY_SECRET);

            if (isValid) {
                return ResponseEntity.ok("Payment verified successfully");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payment signature");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Verification failed: " + e.getMessage());
        }
    }



    /**
     * Get an order by its ID
     * @param orderId The order ID
     * @return ResponseEntity containing the order response
     */
    @Operation(summary = "Get order by ID", description = "Retrieves an order by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order retrieved successfully", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(billingService.getOrder(orderId));
    }

    /**
     * Get all orders for the current user
     * @return ResponseEntity containing a list of order responses
     */
    @Operation(summary = "Get all orders", description = "Retrieves all orders for the current user.")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully", content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders() {
        return ResponseEntity.ok(billingService.getOrders());
    }

    /**
     * Get all orders for the current user with a specific status
     * @param status The order status
     * @return ResponseEntity containing a list of order responses
     */
    @Operation(summary = "Get orders by status", description = "Retrieves all orders for the current user with a specific status.")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully", content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable Status status) {
        return ResponseEntity.ok(billingService.getOrdersByStatus(status));
    }

    /**
     * Generate a PDF bill for an order
     * @param orderId The order ID
     * @return ResponseEntity containing the PDF bill
     */
    @Operation(summary = "Generate PDF bill", description = "Generates a PDF bill for the specified order.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF bill generated successfully", content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
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
    @Operation(summary = "Generate text bill", description = "Generates a text bill for the specified order.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Text bill generated successfully", content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
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

    @Operation(summary = "Delete order", description = "Deletes an order by its ID. Requires ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order deleted successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorsResponse.class)))
    })
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
