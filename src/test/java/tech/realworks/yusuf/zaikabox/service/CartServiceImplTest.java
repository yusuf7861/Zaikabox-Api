package tech.realworks.yusuf.zaikabox.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.realworks.yusuf.zaikabox.entity.CartEntity;
import tech.realworks.yusuf.zaikabox.io.CartRequest;
import tech.realworks.yusuf.zaikabox.io.CartResponse;
import tech.realworks.yusuf.zaikabox.repository.CartRepository;
import tech.realworks.yusuf.zaikabox.repository.userRepo.UserRepository;
import tech.realworks.yusuf.zaikabox.service.userService.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private String userId;
    private String foodId;
    private CartEntity cartEntity;
    private CartRequest cartRequest;
    private Map<String, Integer> cartItems;

    @BeforeEach
    void setUp() {
        userId = "user123";
        foodId = "food123";
        cartItems = new HashMap<>();
        cartItems.put(foodId, 1);

        cartEntity = new CartEntity();
        cartEntity.setId("cart123");
        cartEntity.setUserId(userId);
        cartEntity.setCartItems(new HashMap<>(cartItems));

        cartRequest = new CartRequest();
        cartRequest.setUserId(userId);
        cartRequest.setItems(new HashMap<>(cartItems));

        when(userService.findByUserId()).thenReturn(userId);
    }

    @Test
    void addToCart_WhenCartExists_ShouldAddItemAndReturnUpdatedCart() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cartEntity));
        when(cartRepository.save(any(CartEntity.class))).thenReturn(cartEntity);

        // Act
        CartResponse response = cartService.addToCart(foodId);

        // Assert
        assertNotNull(response);
        assertEquals(cartEntity.getId(), response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(2, response.getItems().get(foodId)); // Item quantity should be incremented
        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).save(any(CartEntity.class));
    }

    @Test
    void addToCart_WhenCartDoesNotExist_ShouldCreateCartAddItemAndReturnCart() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(CartEntity.class))).thenReturn(cartEntity);

        // Act
        CartResponse response = cartService.addToCart(foodId);

        // Assert
        assertNotNull(response);
        assertEquals(cartEntity.getId(), response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(1, response.getItems().get(foodId)); // Item quantity should be 1
        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).save(any(CartEntity.class));
    }

    @Test
    void getCart_WhenCartExists_ShouldReturnCart() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cartEntity));

        // Act
        CartResponse response = cartService.getCart();

        // Assert
        assertNotNull(response);
        assertEquals(cartEntity.getId(), response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(cartItems, response.getItems());
        verify(cartRepository).findByUserId(userId);
    }

    @Test
    void getCart_WhenCartDoesNotExist_ShouldReturnEmptyCart() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act
        CartResponse response = cartService.getCart();

        // Assert
        assertNotNull(response);
        assertNull(response.getId()); // Empty cart has no ID
        assertEquals(userId, response.getUserId());
        assertTrue(response.getItems().isEmpty()); // Empty cart has no items
        verify(cartRepository).findByUserId(userId);
    }

    @Test
    void updateCart_WhenCartExists_ShouldUpdateCartAndReturnUpdatedCart() {
        // Arrange
        Map<String, Integer> updatedItems = new HashMap<>();
        updatedItems.put(foodId, 3);
        updatedItems.put("food456", 2);
        
        CartRequest updateRequest = new CartRequest();
        updateRequest.setUserId(userId);
        updateRequest.setItems(updatedItems);
        
        CartEntity updatedEntity = new CartEntity();
        updatedEntity.setId(cartEntity.getId());
        updatedEntity.setUserId(userId);
        updatedEntity.setCartItems(updatedItems);
        
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cartEntity));
        when(cartRepository.save(any(CartEntity.class))).thenReturn(updatedEntity);

        // Act
        CartResponse response = cartService.updateCart(updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(updatedEntity.getId(), response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(updatedItems, response.getItems());
        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).save(any(CartEntity.class));
    }

    @Test
    void updateCart_WhenCartDoesNotExist_ShouldCreateCartWithItemsAndReturnCart() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(CartEntity.class))).thenReturn(cartEntity);

        // Act
        CartResponse response = cartService.updateCart(cartRequest);

        // Assert
        assertNotNull(response);
        assertEquals(cartEntity.getId(), response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(cartItems, response.getItems());
        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).save(any(CartEntity.class));
    }

    @Test
    void removeFromCart_WhenCartExistsAndItemExists_ShouldRemoveItemAndReturnUpdatedCart() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cartEntity));
        
        CartEntity updatedEntity = new CartEntity();
        updatedEntity.setId(cartEntity.getId());
        updatedEntity.setUserId(userId);
        updatedEntity.setCartItems(new HashMap<>()); // Empty cart after removal
        
        when(cartRepository.save(any(CartEntity.class))).thenReturn(updatedEntity);

        // Act
        CartResponse response = cartService.removeFromCart(foodId);

        // Assert
        assertNotNull(response);
        assertEquals(updatedEntity.getId(), response.getId());
        assertEquals(userId, response.getUserId());
        assertTrue(response.getItems().isEmpty()); // Item should be removed
        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).save(any(CartEntity.class));
    }

    @Test
    void removeFromCart_WhenCartDoesNotExist_ShouldReturnEmptyCart() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act
        CartResponse response = cartService.removeFromCart(foodId);

        // Assert
        assertNotNull(response);
        assertNull(response.getId()); // Empty cart has no ID
        assertEquals(userId, response.getUserId());
        assertTrue(response.getItems().isEmpty()); // Empty cart has no items
        verify(cartRepository).findByUserId(userId);
        verify(cartRepository, never()).save(any(CartEntity.class));
    }

    @Test
    void clearCart_WhenCartExists_ShouldClearCartAndReturnEmptyCart() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cartEntity));
        
        CartEntity clearedEntity = new CartEntity();
        clearedEntity.setId(cartEntity.getId());
        clearedEntity.setUserId(userId);
        clearedEntity.setCartItems(new HashMap<>()); // Empty cart after clearing
        
        when(cartRepository.save(any(CartEntity.class))).thenReturn(clearedEntity);

        // Act
        CartResponse response = cartService.clearCart();

        // Assert
        assertNotNull(response);
        assertEquals(clearedEntity.getId(), response.getId());
        assertEquals(userId, response.getUserId());
        assertTrue(response.getItems().isEmpty()); // Cart should be empty
        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).save(any(CartEntity.class));
    }

    @Test
    void clearCart_WhenCartDoesNotExist_ShouldReturnEmptyCart() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act
        CartResponse response = cartService.clearCart();

        // Assert
        assertNotNull(response);
        assertNull(response.getId()); // Empty cart has no ID
        assertEquals(userId, response.getUserId());
        assertTrue(response.getItems().isEmpty()); // Empty cart has no items
        verify(cartRepository).findByUserId(userId);
        verify(cartRepository, never()).save(any(CartEntity.class));
    }
}