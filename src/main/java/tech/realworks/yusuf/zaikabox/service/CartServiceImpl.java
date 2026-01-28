package tech.realworks.yusuf.zaikabox.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.realworks.yusuf.zaikabox.entity.CartEntity;
import tech.realworks.yusuf.zaikabox.io.CartRequest;
import tech.realworks.yusuf.zaikabox.io.CartResponse;
import tech.realworks.yusuf.zaikabox.repository.CartRepository;
import tech.realworks.yusuf.zaikabox.repository.userRepo.UserRepository;
import tech.realworks.yusuf.zaikabox.service.userservice.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public CartResponse addToCart(String foodId) {
        String loggedInUserId = userService.findByUserId();
        Optional<CartEntity> cartOptional = findCartByUserId(loggedInUserId);
        CartEntity cartEntity = cartOptional.orElseGet(() -> new CartEntity(loggedInUserId, new HashMap<>()));
        Map<String, Integer> cartItems = cartEntity.getCartItems();
        if (cartItems == null) {
            cartItems = new HashMap<>(); // Ensure map is initialized
        }
        cartItems.put(foodId, cartItems.getOrDefault(foodId, 0) + 1);
        cartEntity.setCartItems(cartItems);
        cartEntity = cartRepository.save(cartEntity);
        return convertToResponse(cartEntity);
    }

    @Override
    public CartResponse getCart() {
        String loggedInUserId = userService.findByUserId();
        Optional<CartEntity> cartOptional = findCartByUserId(loggedInUserId);
        if (cartOptional.isPresent()) {
            return convertToResponse(cartOptional.get());
        } else {
            // Return an empty cart if the user doesn't have one yet
            CartEntity emptyCart = new CartEntity(loggedInUserId, new HashMap<>());
            return convertToResponse(emptyCart);
        }
    }

    @Override
    public CartResponse updateCart(CartRequest cartRequest) {
        String loggedInUserId = userService.findByUserId();
        Optional<CartEntity> cartOptional = findCartByUserId(loggedInUserId);
        CartEntity cartEntity = cartOptional.orElseGet(() -> new CartEntity(loggedInUserId, new HashMap<>()));
        cartEntity.setCartItems(cartRequest.getItems());
        cartEntity = cartRepository.save(cartEntity);
        return convertToResponse(cartEntity);
    }

    @Override
    public CartResponse removeFromCart(String foodId) {
        String loggedInUserId = userService.findByUserId();
        Optional<CartEntity> cartOptional = findCartByUserId(loggedInUserId);
        if (cartOptional.isPresent()) {
            CartEntity cartEntity = cartOptional.get();
            Map<String, Integer> cartItems = cartEntity.getCartItems();
            if (cartItems != null) {
                cartItems.remove(foodId);
                cartEntity.setCartItems(cartItems);
                cartEntity = cartRepository.save(cartEntity);
            }
            return convertToResponse(cartEntity);
        } else {
            // Return an empty cart if the user doesn't have one yet
            CartEntity emptyCart = new CartEntity(loggedInUserId, new HashMap<>());
            return convertToResponse(emptyCart);
        }
    }

    @Override
    public CartResponse clearCart() {
        String loggedInUserId = userService.findByUserId();
        Optional<CartEntity> cartOptional = findCartByUserId(loggedInUserId);
        if (cartOptional.isPresent()) {
            CartEntity cartEntity = cartOptional.get();
            cartEntity.setCartItems(new HashMap<>());
            cartEntity = cartRepository.save(cartEntity);
            return convertToResponse(cartEntity);
        } else {
            // Return an empty cart if the user doesn't have one yet
            CartEntity emptyCart = new CartEntity(loggedInUserId, new HashMap<>());
            return convertToResponse(emptyCart);
        }
    }

    private CartResponse convertToResponse(CartEntity cartEntity) {
        return CartResponse.builder()
                .id(cartEntity.getId())
                .userId(cartEntity.getUserId())
                .items(cartEntity.getCartItems())
                .build();
    }

    /**
     * Helper method to find cart by userId, handling potential duplicates by merging them.
     */
    private Optional<CartEntity> findCartByUserId(String userId) {
        List<CartEntity> carts = cartRepository.findByUserId(userId);
        if (carts == null || carts.isEmpty()) {
            return Optional.empty();
        }

        if (carts.size() == 1) {
            return Optional.of(carts.get(0));
        }

        // Handle inconsistent data: multiple carts for same user
        // We will merge them into the first one and delete the rest
        CartEntity primaryCart = carts.get(0);
        if (primaryCart.getCartItems() == null) {
            primaryCart.setCartItems(new HashMap<>());
        }

        for (int i = 1; i < carts.size(); i++) {
            CartEntity duplicateCart = carts.get(i);
            if (duplicateCart.getCartItems() != null) {
                for (Map.Entry<String, Integer> entry : duplicateCart.getCartItems().entrySet()) {
                    primaryCart.getCartItems().put(entry.getKey(),
                        primaryCart.getCartItems().getOrDefault(entry.getKey(), 0) + entry.getValue());
                }
            }
            // Delete duplicate
            cartRepository.delete(duplicateCart);
        }

        return Optional.of(cartRepository.save(primaryCart));
    }
}
