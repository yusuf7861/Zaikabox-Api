package tech.realworks.yusuf.zaikabox.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * Represents a shopping cart entity in the ZaikaBox application.
 * This class is used to store cart-related information in the MongoDB database.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "carts")
public class CartEntity {
    @Id
    private String id;
    private String userId; // Reference to the user who owns the cart
    private Map<String, Integer> cartItems; // Map of food item IDs to their quantities in the cart

    public CartEntity(String userId, Map<String, Integer> cartItems) {
        this.userId = userId;
        this.cartItems = cartItems;
    }
}
