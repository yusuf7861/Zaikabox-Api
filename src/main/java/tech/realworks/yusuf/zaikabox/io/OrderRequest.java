package tech.realworks.yusuf.zaikabox.io;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.realworks.yusuf.zaikabox.entity.BillingDetails;

import java.util.List;

/**
 * DTO for order request.
 * Used to receive order creation requests from clients.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    @NotBlank(message = "Payment mode is required")
    private String paymentMode; // Payment mode (e.g., UPI, CARD, COD)
    @Valid
    private List<OrderItemRequest> items; // List of items to order (optional, can use cart items if not provided)
    private boolean useCart; // Flag to indicate whether to use the current cart items
    @Valid
    private BillingDetails billingDetails;
}
