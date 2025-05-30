package tech.realworks.yusuf.zaikabox.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tech.realworks.yusuf.zaikabox.io.CartRequest;
import tech.realworks.yusuf.zaikabox.io.CartResponse;
import tech.realworks.yusuf.zaikabox.service.CartService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private CartResponse cartResponse;
    private CartRequest cartRequest;
    private String userId;
    private String foodId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
        objectMapper = new ObjectMapper();
        
        userId = "user123";
        foodId = "food123";
        
        Map<String, Integer> cartItems = new HashMap<>();
        cartItems.put(foodId, 1);
        
        cartResponse = CartResponse.builder()
                .id("cart123")
                .userId(userId)
                .items(cartItems)
                .build();
                
        cartRequest = CartRequest.builder()
                .userId(userId)
                .items(cartItems)
                .build();
    }

    @Test
    void getCart_ShouldReturnCartResponse() throws Exception {
        // Arrange
        when(cartService.getCart()).thenReturn(cartResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/carts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(cartResponse.getId()))
                .andExpect(jsonPath("$.userId").value(cartResponse.getUserId()))
                .andExpect(jsonPath("$.items." + foodId).value(1));
    }

    @Test
    void addToCart_ShouldReturnUpdatedCartResponse() throws Exception {
        // Arrange
        when(cartService.addToCart(anyString())).thenReturn(cartResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/carts/items/{foodId}", foodId))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(cartResponse.getId()))
                .andExpect(jsonPath("$.userId").value(cartResponse.getUserId()))
                .andExpect(jsonPath("$.items." + foodId).value(1));
    }

    @Test
    void updateCart_ShouldReturnUpdatedCartResponse() throws Exception {
        // Arrange
        when(cartService.updateCart(any(CartRequest.class))).thenReturn(cartResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(cartResponse.getId()))
                .andExpect(jsonPath("$.userId").value(cartResponse.getUserId()))
                .andExpect(jsonPath("$.items." + foodId).value(1));
    }

    @Test
    void removeFromCart_ShouldReturnUpdatedCartResponse() throws Exception {
        // Arrange
        // Create a response with the item removed
        Map<String, Integer> emptyItems = new HashMap<>();
        CartResponse emptyCartResponse = CartResponse.builder()
                .id("cart123")
                .userId(userId)
                .items(emptyItems)
                .build();
                
        when(cartService.removeFromCart(anyString())).thenReturn(emptyCartResponse);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/carts/items/{foodId}", foodId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(emptyCartResponse.getId()))
                .andExpect(jsonPath("$.userId").value(emptyCartResponse.getUserId()))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void clearCart_ShouldReturnEmptyCartResponse() throws Exception {
        // Arrange
        // Create an empty cart response
        Map<String, Integer> emptyItems = new HashMap<>();
        CartResponse emptyCartResponse = CartResponse.builder()
                .id("cart123")
                .userId(userId)
                .items(emptyItems)
                .build();
                
        when(cartService.clearCart()).thenReturn(emptyCartResponse);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/carts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(emptyCartResponse.getId()))
                .andExpect(jsonPath("$.userId").value(emptyCartResponse.getUserId()))
                .andExpect(jsonPath("$.items").isEmpty());
    }
}