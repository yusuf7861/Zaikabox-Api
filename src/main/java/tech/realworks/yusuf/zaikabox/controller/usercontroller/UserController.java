package tech.realworks.yusuf.zaikabox.controller.usercontroller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.realworks.yusuf.zaikabox.io.user.UserResponse;
import tech.realworks.yusuf.zaikabox.service.userservice.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/users"})
@Tag(name = "User", description = "Authenticated user self-service APIs")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get user profile", description = "Retrieves the profile of the currently authenticated user.")
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getUserProfile() {
        UserResponse userProfile = userService.getUserProfile();
        return ResponseEntity.ok(userProfile);
    }
}
