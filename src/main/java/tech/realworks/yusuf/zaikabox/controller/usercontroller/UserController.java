package tech.realworks.yusuf.zaikabox.controller.usercontroller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tech.realworks.yusuf.zaikabox.entity.UserEntity;
import tech.realworks.yusuf.zaikabox.io.ErrorsResponse;
import tech.realworks.yusuf.zaikabox.io.user.ResetPasswordRequest;
import tech.realworks.yusuf.zaikabox.io.user.SendResetOtpRequest;
import tech.realworks.yusuf.zaikabox.io.user.VerifyOtpRequest;
import tech.realworks.yusuf.zaikabox.io.user.AuthenticationRequest;
import tech.realworks.yusuf.zaikabox.io.user.AuthenticationResponse;
import tech.realworks.yusuf.zaikabox.io.user.UserRequest;
import tech.realworks.yusuf.zaikabox.io.user.UserResponse;
import tech.realworks.yusuf.zaikabox.repository.userRepo.UserRepository;
import tech.realworks.yusuf.zaikabox.service.userservice.AppUserDetailsService;
import tech.realworks.yusuf.zaikabox.service.userservice.UserService;
import tech.realworks.yusuf.zaikabox.util.JwtUtil;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/users"})
@Tag(name = "User Management", description = "APIs for user registration, authentication, and management")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Operation(summary = "Register a new user", description = "Registers a new user account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email already exists", content = @Content(schema = @Schema(implementation = ErrorsResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequest userRequest) {
        try {
            if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorsResponse("Email already exists", HttpStatus.BAD_REQUEST));
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(userRequest));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorsResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ErrorsResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authRequest){
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());

            final String token = jwtUtil.generateToken(userDetails); // Pass userDetails to generate the token
            ResponseCookie cookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .secure(true) // must be true if using sameSite=None
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("none") // allow cross origin cookies
                    .build();

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthenticationResponse(authRequest.getEmail(), token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorsResponse("Invalid credentials", HttpStatus.UNAUTHORIZED));
        }
    }

    @Operation(summary = "Get user profile", description = "Retrieves the profile of the currently authenticated user.")
    @ApiResponse(responseCode = "200", description = "User profile retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getUserProfile() {
        UserResponse userProfile = userService.getUserProfile();
        return ResponseEntity.ok(userProfile);
    }

    @Operation(summary = "Check authentication status", description = "Checks if the user is authenticated.")
    @ApiResponse(responseCode = "200", description = "Authentication status returned")
    @GetMapping("/is-authenticated")
    public ResponseEntity<?> isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && 
                                 !authentication.getPrincipal().equals("anonymousUser");

        HashMap<String, Object> response = new HashMap<>();
        response.put("isAuthenticated", isAuthenticated);

        if (isAuthenticated) {
            response.put("username", authentication.getName());
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout user", description = "Logs out the currently authenticated user.")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Clear the authentication from the security context
        SecurityContextHolder.clearContext();

        // Create a cookie with the same name but with max age 0 to delete it
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite("none")
                .build();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @Operation(summary = "Delete user", description = "Deletes the currently authenticated user. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorsResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public ResponseEntity<?> deleteUser() {
        try {
            userService.deleteUser();
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorsResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Operation(summary = "Get all users (admin only)", description = "Retrieves all users. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserEntity> users = userRepository.findAll();
            List<UserResponse> userResponses = users.stream()
                    .map(user -> UserResponse.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorsResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Operation(summary = "Send password reset OTP", description = "Sends a password reset OTP to the user's email.")
    @ApiResponse(responseCode = "200", description = "OTP sent successfully")
    @PostMapping("/send-reset-otp")
    public ResponseEntity<?> sendResetPasswordOTP(@RequestBody SendResetOtpRequest request) {
        try {
            String email = request.getEmail();
            String token = userService.sendPasswordResetEmail(email);
            return ResponseEntity.ok(Map.of("token", token, "message", "OTP sent successfully"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Operation(summary = "Verify OTP", description = "Verifies the OTP for password reset.")
    @ApiResponse(responseCode = "200", description = "OTP verified successfully")
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        try {
            String token = request.getToken();
            String otp = request.getOtp();
            boolean valid = userService.verifyOtp(token, otp);
            return valid ? ResponseEntity.ok(Map.of("message", "OTP verified successfully")) : ResponseEntity.badRequest().body(Map.of("message", "Invalid OTP"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Operation(summary = "Reset password", description = "Resets the user's password using a valid OTP token.")
    @ApiResponse(responseCode = "200", description = "Password reset successfully")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            String token = request.getToken();
            String password = request.getPassword();
            userService.resetPassword(token, password);
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
