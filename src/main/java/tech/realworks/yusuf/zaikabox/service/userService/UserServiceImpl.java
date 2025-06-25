package tech.realworks.yusuf.zaikabox.service.userService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.realworks.yusuf.zaikabox.entity.Role;
import tech.realworks.yusuf.zaikabox.entity.UserEntity;
import tech.realworks.yusuf.zaikabox.io.user.UserRequest;
import tech.realworks.yusuf.zaikabox.io.user.UserResponse;
import tech.realworks.yusuf.zaikabox.repository.CartRepository;
import tech.realworks.yusuf.zaikabox.repository.userRepo.UserRepository;
import tech.realworks.yusuf.zaikabox.util.JwtOtpUtil;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationFacade authenticationFacade;
    private final CartRepository cartRepository;
    private final JavaMailSender javaMailSender;
    private final JwtOtpUtil jwtOtpUtil;

    @Override
    public UserResponse registerUser(UserRequest userRequest) {
        UserEntity newUser = convertToEntity(userRequest);
        newUser = userRepository.save(newUser);
        return convertToResponse(newUser);
    }

    @Override
    public String findByUserId() {
        String loggedInUserEmail = authenticationFacade.getAuthentication().getName();
        UserEntity loggedInUser = userRepository
                .findByEmail(loggedInUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + loggedInUserEmail + " not found"));
        return loggedInUser.getId();
    }

    @Override
    public UserResponse getUserProfile() {
        String loggedInUserEmail = authenticationFacade.getAuthentication().getName();
        UserEntity loggedInUser = userRepository
                .findByEmail(loggedInUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + loggedInUserEmail + " not found"));
        return convertToResponse(loggedInUser);
    }

    @Override
    public void deleteUser() {
        String loggedInUserEmail = authenticationFacade.getAuthentication().getName();
        UserEntity loggedInUser = userRepository
                .findByEmail(loggedInUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + loggedInUserEmail + " not found"));

        // Delete the user's cart first
        cartRepository.deleteByUserId(loggedInUser.getId());

        // Then delete the user
        userRepository.delete(loggedInUser);
    }

    @Override
    public String sendPasswordResetEmail(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));

        String resetOtp = String.format("%06d", new Random().nextInt(999999));
        String token = jwtOtpUtil.generateOtpToken(email, resetOtp, 2);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("yjamal12feb@gmail.com");
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject("Password Reset OTP - Zaikabox");
        simpleMailMessage.setText("Your password reset OTP is " + resetOtp + ". It is valid for 2 minutes.");
        javaMailSender.send(simpleMailMessage);
        return token;
    }

    @Override
    public boolean verifyOtp(String token, String otpInput) {
        try {
            Claims claims = jwtOtpUtil.validateTokenAndGetClaims(token);
            String otpFromToken = claims.get("otp", String.class);
            return otpInput.equals(otpFromToken);
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("OTP has expired");
        } catch (Exception e) {
            throw new RuntimeException("Invalid OTP");
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        try {
            Claims claims = jwtOtpUtil.validateTokenAndGetClaims(token);
            String email = claims.getSubject();
            UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
    }

    private UserEntity convertToEntity(UserRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(Role.CUSTOMER) // Set default role as CUSTOMER
                .build();
    }

    private UserResponse convertToResponse(UserEntity registeredUser) {
        return UserResponse.builder()
                .id(registeredUser.getId())
                .name(registeredUser.getName())
                .email(registeredUser.getEmail())
                .role(registeredUser.getRole())
                .build();
    }
}
