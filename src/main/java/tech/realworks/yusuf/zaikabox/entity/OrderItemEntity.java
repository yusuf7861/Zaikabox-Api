package tech.realworks.yusuf.zaikabox.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an order item entity in the ZaikaBox application.
 * This class is used as a nested document within OrderEntity to store information about individual items in an order.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemEntity {
    private String foodId; // Reference to the food item
    private String name; // Name of the food item
    private int quantity; // Quantity of the food item
    private double unitPrice; // Price per unit
    private double total; // Total price for this item (quantity * unitPrice)
}