package tech.realworks.yusuf.zaikabox.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tech.realworks.yusuf.zaikabox.entity.Role;
import tech.realworks.yusuf.zaikabox.entity.UserEntity;
import tech.realworks.yusuf.zaikabox.io.ErrorsResponse;
import tech.realworks.yusuf.zaikabox.io.user.AdminProfileResponse;
import tech.realworks.yusuf.zaikabox.io.user.AuthenticationRequest;
import tech.realworks.yusuf.zaikabox.io.user.AuthenticationResponse;
import tech.realworks.yusuf.zaikabox.io.user.UserResponse;
import tech.realworks.yusuf.zaikabox.repository.userRepo.UserRepository;
import tech.realworks.yusuf.zaikabox.service.AuditService;
import tech.realworks.yusuf.zaikabox.service.userservice.AppUserDetailsService;
import tech.realworks.yusuf.zaikabox.service.userservice.UserService;
import tech.realworks.yusuf.zaikabox.util.JwtUtil;

import java.time.Duration;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Admin only endpoints")
public class AdminController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AuditService auditService;


    @Operation(summary = "Admin login", description = "Authenticates an admin and returns a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or not an admin", content = @Content(schema = @Schema(implementation = ErrorsResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authRequest){
        try {
            Optional<UserEntity> userOpt = userRepository.findByEmail(authRequest.getEmail());

            if (userOpt.isEmpty()) {
                 auditService.logLoginEvent(null, authRequest.getEmail(), false, "Admin login failed: User not found");
                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorsResponse("Invalid email or password", HttpStatus.UNAUTHORIZED));
            }

            if (userOpt.get().getRole() != Role.ADMIN) {
                 auditService.logLoginEvent(userOpt.get().getId(), authRequest.getEmail(), false, "Admin login failed: User is not admin");
                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorsResponse("Access Denied: Not an admin", HttpStatus.UNAUTHORIZED));
            }

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());

            final String token = jwtUtil.generateToken(userDetails);
            ResponseCookie cookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("none")
                    .build();

            auditService.logLoginEvent(userOpt.get().getId(), authRequest.getEmail(), true, "Admin logged in successfully");

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthenticationResponse(token, authRequest.getEmail()));

        } catch (Exception e) {
            auditService.logLoginEvent(null, authRequest.getEmail(), false, "Admin login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorsResponse("Invalid email or password", HttpStatus.UNAUTHORIZED));
        }
    }

    @Operation(summary = "Get admin profile", description = "Retrieves the profile of the currently authenticated admin.")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", content = @Content(schema = @Schema(implementation = AdminProfileResponse.class)))
    @GetMapping("/profile")
    public ResponseEntity<AdminProfileResponse> getProfile(){
        UserResponse userResponse = userService.getUserProfile();
        AdminProfileResponse response = AdminProfileResponse.builder()
                .name(userResponse.getName())
                .email(userResponse.getEmail())
                .role(userResponse.getRole())
                .build();
        return ResponseEntity.ok(response);
    }
}
