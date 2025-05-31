package tech.realworks.yusuf.zaikabox.service.userService;

import tech.realworks.yusuf.zaikabox.io.user.UserRequest;
import tech.realworks.yusuf.zaikabox.io.user.UserResponse;

public interface UserService {
    UserResponse registerUser(UserRequest userRequest);
    String findByUserId();
    UserResponse getUserProfile();
    void deleteUser();
}
