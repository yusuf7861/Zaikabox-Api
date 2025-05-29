package tech.realworks.yusuf.zaikabox.service.userService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import tech.realworks.yusuf.zaikabox.io.user.UserRequest;
import tech.realworks.yusuf.zaikabox.repository.userRepo.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Mock
    UserService userService;

    @InjectMocks
    UserRepository userRepository;

    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        UserRequest userRequest1 = new UserRequest();
        userRequest1.setId("1234");
        userRequest1.setName("Yusuf");
        userRequest1.setEmail("yjamal710@gmail.com");
        userRequest1.setPassword("yusuf7861");
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void registerUser() {

    }

    @Test
    void findByUserId() {
    }
}