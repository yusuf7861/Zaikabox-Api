package tech.realworks.yusuf.zaikabox.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.realworks.yusuf.zaikabox.entity.CartEntity;
import tech.realworks.yusuf.zaikabox.io.CartRequest;
import tech.realworks.yusuf.zaikabox.io.CartResponse;
import tech.realworks.yusuf.zaikabox.repository.CartRepository;
import tech.realworks.yusuf.zaikabox.repository.userRepo.UserRepository;
import tech.realworks.yusuf.zaikabox.service.userService.UserService;

import java.util.HashMap;
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
        Optional<CartEntity> cartOptional = cartRepository.findByUserId(loggedInUserId);
        CartEntity cartEntity = cartOptional.orElseGet(() -> new CartEntity(loggedInUserId, new HashMap<>()));
        Map<String, Integer> cartItems = cartEntity.getCartItems();
        cartItems.put(foodId, cartItems.getOrDefault(foodId, 0) + 1);
        cartEntity.setCartItems(cartItems);
        cartEntity = cartRepository.save(cartEntity);
        return convertToResponse(cartEntity);
    }

    @Override
    public CartResponse getCart() {
        String loggedInUserId = userService.findByUserId();
        Optional<CartEntity> cartOptional = cartRepository.findByUserId(loggedInUserId);
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
        Optional<CartEntity> cartOptional = cartRepository.findByUserId(loggedInUserId);
        CartEntity cartEntity = cartOptional.orElseGet(() -> new CartEntity(loggedInUserId, new HashMap<>()));
        cartEntity.setCartItems(cartRequest.getItems());
        cartEntity = cartRepository.save(cartEntity);
        return convertToResponse(cartEntity);
    }

    @Override
    public CartResponse removeFromCart(String foodId) {
        String loggedInUserId = userService.findByUserId();
        Optional<CartEntity> cartOptional = cartRepository.findByUserId(loggedInUserId);
        if (cartOptional.isPresent()) {
            CartEntity cartEntity = cartOptional.get();
            Map<String, Integer> cartItems = cartEntity.getCartItems();
            cartItems.remove(foodId);
            cartEntity.setCartItems(cartItems);
            cartEntity = cartRepository.save(cartEntity);
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
        Optional<CartEntity> cartOptional = cartRepository.findByUserId(loggedInUserId);
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
}
