package tech.realworks.yusuf.zaikabox.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for order item response.
 * Used as a nested object within OrderResponse.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemResponse {
    private String name; // Name of the food item
    private int quantity; // Quantity of the food item
    private double unitPrice; // Price per unit
    private double total; // Total price for this item (quantity * unitPrice)
}