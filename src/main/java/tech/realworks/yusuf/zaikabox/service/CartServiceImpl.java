package tech.realworks.yusuf.zaikabox.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.realworks.yusuf.zaikabox.entity.CartEntity;
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
    
//    @Override
//    public void addToCart(String foodId) {
//        String loggedInUserId = userService.findByUserId();
//        Optional<CartEntity> cartOptional = cartRepository.findByUserId(loggedInUserId);
//        CartEntity cartEntity = cartOptional.orElseGet(() -> new CartEntity(loggedInUserId, new HashMap<>()));
//        Map<String, Integer> cartItems = cartEntity.getCartItems();
//        cartItems.put(foodId, cartItems.getOrDefault(foodId, 0) + 1);
//        cartEntity.setCartItems(cartItems);
//        cartEntity = cartRepository.save(cartEntity);
//        convertToResponse(cartEntity);
//    }

    private void convertToResponse(CartEntity cartEntity) {
    }
}
