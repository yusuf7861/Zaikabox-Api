package tech.realworks.yusuf.zaikabox.controller.usercontroller;

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
import tech.realworks.yusuf.zaikabox.service.AuditService;
import tech.realworks.yusuf.zaikabox.service.userservice.AppUserDetailsService;
import tech.realworks.yusuf.zaikabox.service.userservice.UserService;
import tech.realworks.yusuf.zaikabox.util.JwtUtil;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/users"})
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequest userRequest) {
        try {
            if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
                // Log failed registration attempt due to existing email
                auditService.logRegistrationEvent(null, userRequest.getEmail(), false, "Email already exists");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorsResponse("Email already exists", HttpStatus.BAD_REQUEST));
            } else {
                UserResponse response = userService.registerUser(userRequest);

                // Log successful registration
                auditService.logRegistrationEvent(response.getId(), response.getEmail(), true, "User registered successfully");

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }
        } catch (Exception e) {
            // Log failed registration attempt due to other errors
            auditService.logRegistrationEvent(null, userRequest.getEmail(), false, "Registration failed: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorsResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authRequest){
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());

            // Get user ID for audit logging
            Optional<UserEntity> userOpt = userRepository.findByEmail(authRequest.getEmail());
            String userId = userOpt.isPresent() ? userOpt.get().getId() : null;

            final String token = jwtUtil.generateToken(userDetails); // Pass userDetails to generate the token
            ResponseCookie cookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .secure(true) // must be true if using sameSite=None
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("none") // allow cross origin cookies
                    .build();

            // Log successful login
            auditService.logLoginEvent(userId, authRequest.getEmail(), true, "User logged in successfully");

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthenticationResponse(authRequest.getEmail(), token));
        } catch (AuthenticationException e) {
            // Log failed login attempt
            auditService.logLoginEvent(null, authRequest.getEmail(), false, "Invalid credentials");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorsResponse("Invalid credentials", HttpStatus.UNAUTHORIZED));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getUserProfile() {
        UserResponse userProfile = userService.getUserProfile();
        return ResponseEntity.ok(userProfile);
    }

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

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Get current user info for audit logging
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? authentication.getName() : null;

        // Find user ID if we have the email
        String userId = null;
        if (email != null && !email.equals("anonymousUser")) {
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            userId = userOpt.isPresent() ? userOpt.get().getId() : null;

            // Log logout event using specialized method
        }

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
        auditService.logLogoutEvent(userId, email);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

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

    /**
     * Endpoint to set a user as admin. This endpoint is restricted to admin users only.
     * @param email The email of the user to be set as admin
     * @return ResponseEntity with success or error message
     */
//    @PutMapping("/admin/set-role")
//    public ResponseEntity<?> setUserRole(@RequestParam String email, @RequestParam Role role) {
//        try {
//            UserEntity user = userRepository.findByEmail(email)
//                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
//
//            user.setRole(role);
//            userRepository.save(user);
//
//            Map<String, String> response = new HashMap<>();
//            response.put("message", "User role updated successfully to " + role);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ErrorsResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
//        }
//    }

    /**
     * Admin-only endpoint to get all users. This endpoint is restricted to admin users only.
     * @return ResponseEntity with list of all users
     */
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

    @PostMapping("/send-reset-otp")
    public ResponseEntity<?> sendResetPasswordOTP(@RequestBody SendResetOtpRequest request) {
        try {
            String email = request.getEmail();
            String token = userService.sendPasswordResetEmail(email);

            // Log password reset OTP sent event
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            String userId = userOpt.isPresent() ? userOpt.get().getId() : null;
            auditService.logPasswordResetEvent(userId, email, true, "Password reset OTP sent");

            return ResponseEntity.ok(Map.of("token", token, "message", "OTP sent successfully"));
        } catch (Exception e) {
            // Log failed attempt to send password reset OTP
            auditService.logPasswordResetEvent(null, request.getEmail(), false, "Failed to send OTP: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        try {
            String token = request.getToken();
            String otp = request.getOtp();
            boolean valid = userService.verifyOtp(token, otp);

            // Extract email from token (using the correct method name)
            String email = jwtUtil.extractEmail(token);
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            String userId = userOpt.isPresent() ? userOpt.get().getId() : null;

            if (valid) {
                // Log successful OTP verification
                auditService.logPasswordResetEvent(userId, email, true, "OTP verified successfully");
                return ResponseEntity.ok(Map.of("message", "OTP verified successfully"));
            } else {
                // Log failed OTP verification
                auditService.logPasswordResetEvent(userId, email, false, "Invalid OTP provided");
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid OTP"));
            }
        } catch (Exception e) {
            // Log error during OTP verification
            auditService.logPasswordResetEvent(null, "unknown", false, "Error during OTP verification: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            String token = request.getToken();
            String password = request.getPassword();

            // Extract email from token using the correct method name
            String email = jwtUtil.extractEmail(token);
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            String userId = userOpt.isPresent() ? userOpt.get().getId() : null;

            userService.resetPassword(token, password);

            // Log successful password reset
            auditService.logPasswordResetEvent(userId, email, true, "Password reset successfully");

            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (Exception e) {
            // Log failed password reset
            auditService.logPasswordResetEvent(null, "unknown", false, "Password reset failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
