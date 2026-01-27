package tech.realworks.yusuf.zaikabox.controller.auth;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tech.realworks.yusuf.zaikabox.entity.UserEntity;
import tech.realworks.yusuf.zaikabox.io.ErrorsResponse;
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
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "APIs for user registration, authentication lifecycle")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Operation(summary = "Register a new user", description = "Registers a new user account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email already exists", content = @Content(schema = @Schema(implementation = ErrorsResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequest userRequest) {
        try {
            if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
                auditService.logRegistrationEvent(null, userRequest.getEmail(), false, "Email already exists");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorsResponse("Email already exists", HttpStatus.BAD_REQUEST));
            } else {
                UserResponse response = userService.registerUser(userRequest);
                auditService.logRegistrationEvent(response.getId(), response.getEmail(), true, "User registered successfully");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }
        } catch (Exception e) {
            auditService.logRegistrationEvent(null, userRequest.getEmail(), false, "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorsResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
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

            Optional<UserEntity> userOpt = userRepository.findByEmail(authRequest.getEmail());
            String userId = userOpt.isPresent() ? userOpt.get().getId() : null;

            final String token = jwtUtil.generateToken(userDetails);
            ResponseCookie cookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("none")
                    .build();

            auditService.logLoginEvent(userId, authRequest.getEmail(), true, "User logged in successfully");

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthenticationResponse(token, authRequest.getEmail()));
        } catch (AuthenticationException e) {
            auditService.logLoginEvent(null, authRequest.getEmail(), false, "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorsResponse("Invalid credentials", HttpStatus.UNAUTHORIZED));
        }
    }

    @Operation(summary = "Check authentication status", description = "Checks if the user is authenticated.")
    @ApiResponse(responseCode = "200", description = "Authentication status returned")
    @GetMapping("/is-authenticated")
    public ResponseEntity<?> isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal());
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? authentication.getName() : null;

        String userId = null;
        if (email != null && !"anonymousUser".equals(email)) {
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            userId = userOpt.isPresent() ? userOpt.get().getId() : null;
        }

        SecurityContextHolder.clearContext();

        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("none")
                .build();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        auditService.logLogoutEvent(userId, email);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }
}
