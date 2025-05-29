package tech.realworks.yusuf.zaikabox.controller.usercontroller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tech.realworks.yusuf.zaikabox.controller.ErrorsResponse;
import tech.realworks.yusuf.zaikabox.io.user.AuthenticationRequest;
import tech.realworks.yusuf.zaikabox.io.user.AuthenticationResponse;
import tech.realworks.yusuf.zaikabox.io.user.UserRequest;
import tech.realworks.yusuf.zaikabox.io.user.UserResponse;
import tech.realworks.yusuf.zaikabox.repository.userRepo.UserRepository;
import tech.realworks.yusuf.zaikabox.service.userService.AppUserDetailsService;
import tech.realworks.yusuf.zaikabox.service.userService.UserService;
import tech.realworks.yusuf.zaikabox.util.JwtUtil;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/users"})
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequest userRequest) {
        try {
            if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorsResponse("Email already exists", HttpStatus.BAD_REQUEST));
            } else {
                return ResponseEntity.ok(userService.registerUser(userRequest));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorsResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authRequest){
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

    }
}
