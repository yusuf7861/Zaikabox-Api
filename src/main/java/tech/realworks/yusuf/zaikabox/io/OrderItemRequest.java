package tech.realworks.yusuf.zaikabox.io;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for order item request.
 * Used as a nested object within OrderRequest.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemRequest {
    @NotBlank(message = "Food ID is required")
    private String foodId; // ID of the food item
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity; // Quantity of the food item
}
