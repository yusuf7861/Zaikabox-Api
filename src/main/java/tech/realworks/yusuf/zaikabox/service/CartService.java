package tech.realworks.yusuf.zaikabox.service;

import tech.realworks.yusuf.zaikabox.io.CartRequest;
import tech.realworks.yusuf.zaikabox.io.CartResponse;

public interface CartService {

    /**
     * Adds a food item to the user's cart
     * @param foodId The ID of the food item to add
     * @return The updated cart response
     */
    CartResponse addToCart(String foodId);

    /**
     * Gets the user's cart
     * @return The cart response
     */
    CartResponse getCart();

    /**
     * Updates the user's cart
     * @param cartRequest The cart request containing the updated cart items
     * @return The updated cart response
     */
    CartResponse updateCart(CartRequest cartRequest);

    /**
     * Removes a food item from the user's cart
     * @param foodId The ID of the food item to remove
     * @return The updated cart response
     */
    CartResponse removeFromCart(String foodId);

    /**
     * Clears the user's cart
     * @return The empty cart response
     */
    CartResponse clearCart();

    /**
     * Clears the cart for a specific user.
     * Used by internal processes (e.g. webhook) where security context is not available.
     * @param userId The user ID
     */
    void clearCart(String userId);
}
