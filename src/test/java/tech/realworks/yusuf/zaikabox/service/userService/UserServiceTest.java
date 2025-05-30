package tech.realworks.yusuf.zaikabox.service.userService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import tech.realworks.yusuf.zaikabox.entity.UserEntity;
import tech.realworks.yusuf.zaikabox.io.user.UserRequest;
import tech.realworks.yusuf.zaikabox.io.user.UserResponse;
import tech.realworks.yusuf.zaikabox.repository.userRepo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest();
        userRequest.setId("1234");
        userRequest.setName("Yusuf");
        userRequest.setEmail("yjamal710@gmail.com");
        userRequest.setPassword("yusuf7861");
    }

    @AfterEach
    void tearDown() {
        // Clean up resources if needed
    }

    @Test
    void registerUser() {
        // Arrange
        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(userRequest.getId());
        savedEntity.setName(userRequest.getName());
        savedEntity.setEmail(userRequest.getEmail());
        savedEntity.setPassword("yusuf7861");

        when(passwordEncoder.encode(anyString())).thenReturn("yusuf7861");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        // Act
        UserResponse response = userService.registerUser(userRequest);

        // Assert
        assertNotNull(response);
        assertEquals(userRequest.getId(), response.getId());
        assertEquals(userRequest.getName(), response.getName());
        assertEquals(userRequest.getEmail(), response.getEmail());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void findByUserId() {
        // Arrange
        String email = "yjamal710@gmail.com";
        UserEntity userEntity = new UserEntity();
        userEntity.setId("1234");
        userEntity.setEmail(email);

        // Mock Authentication object
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);
        when(authenticationFacade.getAuthentication()).thenReturn(authentication);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // Act
        String userId = userService.findByUserId();

        // Assert
        assertEquals("1234", userId);
        verify(userRepository).findByEmail(email);
    }


}
