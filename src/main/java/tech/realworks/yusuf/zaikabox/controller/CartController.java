package tech.realworks.yusuf.zaikabox.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Shopping Cart", description = "APIs for managing shopping cart operations")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get user's cart", description = "Retrieves the current user's shopping cart")
    @ApiResponse(responseCode = "200", description = "Cart retrieved successfully",
                 content = @Content(schema = @Schema(implementation = CartResponse.class)))
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    /**
     * Add a food item to the cart
     * @param foodId The ID of the food item to add
     * @return ResponseEntity containing the updated cart response
     */
    @Operation(summary = "Add item to cart", description = "Adds a food item to the user's shopping cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Item successfully added to cart",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
        @ApiResponse(responseCode = "404", description = "Food item not found")
    })
    @PostMapping("/items/{foodId}")
    public ResponseEntity<CartResponse> addToCart(
            @Parameter(description = "ID of the food item to add to cart") @PathVariable String foodId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(foodId));
    }

    /**
     * Update the cart with new items
     * @param cartRequest The cart request containing the updated cart items
     * @return ResponseEntity containing the updated cart response
     */
    @Operation(summary = "Update cart", description = "Updates the user's cart with new items and quantities")
    @ApiResponse(responseCode = "200", description = "Cart successfully updated",
                 content = @Content(schema = @Schema(implementation = CartResponse.class)))
    @PutMapping
    public ResponseEntity<CartResponse> updateCart(
            @Parameter(description = "Cart details with updated items") @RequestBody CartRequest cartRequest) {
        return ResponseEntity.ok(cartService.updateCart(cartRequest));
    }

    /**
     * Remove a food item from the cart
     * @param foodId The ID of the food item to remove
     * @return ResponseEntity containing the updated cart response
     */
    @Operation(summary = "Remove item from cart", description = "Removes a specific food item from the user's shopping cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item successfully removed from cart",
                     content = @Content(schema = @Schema(implementation = CartResponse.class))),
        @ApiResponse(responseCode = "404", description = "Item not found in cart")
    })
    @DeleteMapping("/items/{foodId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @Parameter(description = "ID of the food item to remove from cart") @PathVariable String foodId) {
        return ResponseEntity.ok(cartService.removeFromCart(foodId));
    }

    /**
     * Clear the cart
     * @return ResponseEntity containing the empty cart response
     */
    @Operation(summary = "Clear cart", description = "Removes all items from the user's shopping cart")
    @ApiResponse(responseCode = "200", description = "Cart successfully cleared",
                 content = @Content(schema = @Schema(implementation = CartResponse.class)))
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart() {
        return ResponseEntity.ok(cartService.clearCart());
    }
}
