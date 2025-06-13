package tech.realworks.yusuf.zaikabox.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.realworks.yusuf.zaikabox.io.CartRequest;
import tech.realworks.yusuf.zaikabox.io.CartResponse;
import tech.realworks.yusuf.zaikabox.service.CartService;

/**
 * Controller for handling cart-related operations
 */
@RestController
@RequestMapping({"/api/v1/carts"})
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    /**
     * Add a food item to the cart
     * @param foodId The ID of the food item to add
     * @return ResponseEntity containing the updated cart response
     */
    @PostMapping("/items/{foodId}")
    public ResponseEntity<CartResponse> addToCart(@PathVariable String foodId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(foodId));
    }

    /**
     * Update the cart with new items
     * @param cartRequest The cart request containing the updated cart items
     * @return ResponseEntity containing the updated cart response
     */
    @PutMapping
    public ResponseEntity<CartResponse> updateCart(@RequestBody CartRequest cartRequest) {
        return ResponseEntity.ok(cartService.updateCart(cartRequest));
    }

    /**
     * Remove a food item from the cart
     * @param foodId The ID of the food item to remove
     * @return ResponseEntity containing the updated cart response
     */
    @DeleteMapping("/items/{foodId}")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable String foodId) {
        return ResponseEntity.ok(cartService.removeFromCart(foodId));
    }

    /**
     * Clear the cart
     * @return ResponseEntity containing the empty cart response
     */
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart() {
        return ResponseEntity.ok(cartService.clearCart());
    }
}
