package tech.realworks.yusuf.zaikabox.service.userService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.realworks.yusuf.zaikabox.entity.Role;
import tech.realworks.yusuf.zaikabox.io.user.UserRequest;
import tech.realworks.yusuf.zaikabox.io.user.UserResponse;
import tech.realworks.yusuf.zaikabox.util.JwtOtpUtil;

class UserServiceImplTest {


    UserServiceImpl userServiceImpl = new UserServiceImpl(null, null, new AuthenticationFacadeImpl(), null, null, new JwtOtpUtil());

    @Test
    void testRegisterUser() {
        UserResponse result = userServiceImpl.registerUser(new UserRequest("id", "name", "email", "password"));
        Assertions.assertEquals(new UserResponse("id", "name", "email", Role.ADMIN), result);
    }

    @Test
    void testFindByUserId() {
        String result = userServiceImpl.findByUserId();
        Assertions.assertEquals("replaceMeWithExpectedResult", result);
    }

    @Test
    void testGetUserProfile() {
        UserResponse result = userServiceImpl.getUserProfile();
        Assertions.assertEquals(new UserResponse("id", "name", "email", Role.ADMIN), result);
    }

    @Test
    void testDeleteUser() {
        userServiceImpl.deleteUser();
    }

    @Test
    void testSendPasswordResetEmail() {
        String result = userServiceImpl.sendPasswordResetEmail("email");
        Assertions.assertEquals("replaceMeWithExpectedResult", result);
    }

    @Test
    void testResetPassword() {
        userServiceImpl.resetPassword();
    }
}
