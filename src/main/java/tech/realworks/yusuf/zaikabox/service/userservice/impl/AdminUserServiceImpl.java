package tech.realworks.yusuf.zaikabox.service.userservice.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tech.realworks.yusuf.zaikabox.entity.UserEntity;
import tech.realworks.yusuf.zaikabox.io.ErrorsResponse;
import tech.realworks.yusuf.zaikabox.io.user.AdminResetPasswordRequest;
import tech.realworks.yusuf.zaikabox.io.user.SendResetOtpRequest;
import tech.realworks.yusuf.zaikabox.io.user.UserResponse;
import tech.realworks.yusuf.zaikabox.io.user.VerifyAdminOtpRequest;
import tech.realworks.yusuf.zaikabox.repository.userRepo.UserRepository;
import tech.realworks.yusuf.zaikabox.service.AuditService;
import tech.realworks.yusuf.zaikabox.service.userservice.AdminPasswordResetService;
import tech.realworks.yusuf.zaikabox.service.userservice.AdminUserService;
import tech.realworks.yusuf.zaikabox.service.userservice.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AdminPasswordResetService adminPasswordResetService;
    private final AuditService auditService;

    @Override
    public List<UserResponse> getAllUsers() {
        List<UserEntity> users = userRepository.findAll();
        return users.stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> deleteCurrentUser() {
        userService.deleteUser();
        return Map.of("message", "User deleted successfully");
    }

    @Override
    public Map<String, String> sendAdminPasswordResetOtp(SendResetOtpRequest request) {
        String email = request.getEmail();
        try {
            adminPasswordResetService.sendOtp(email);
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            String userId = userOpt.map(UserEntity::getId).orElse(null);
            auditService.logPasswordResetEvent(userId, email, true, "Admin password reset OTP sent");
            return Map.of("message", "OTP sent to your email");
        } catch (UsernameNotFoundException e) {
            return Map.of("status", String.valueOf(HttpStatus.NOT_FOUND.value()), "message", e.getMessage());
        } catch (Exception e) {
            auditService.logPasswordResetEvent(null, email, false, "Failed to send OTP: " + e.getMessage());
            return Map.of("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "message", "Failed to send OTP");
        }
    }

    @Override
    public Map<String, String> verifyAdminOtp(VerifyAdminOtpRequest request) {
        String email = request.getEmail();
        try {
            adminPasswordResetService.verifyOtp(email, request.getOtp());
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            String userId = userOpt.map(UserEntity::getId).orElse(null);
            auditService.logPasswordResetEvent(userId, email, true, "OTP verified");
            return Map.of("message", "OTP Verified");
        } catch (IllegalArgumentException e) {
            auditService.logPasswordResetEvent(null, email, false, "OTP verification failed: " + e.getMessage());
            return Map.of("status", String.valueOf(HttpStatus.BAD_REQUEST.value()), "message", e.getMessage());
        } catch (Exception e) {
            auditService.logPasswordResetEvent(null, email, false, "OTP verification failed: " + e.getMessage());
            return Map.of("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "message", "OTP verification failed");
        }
    }

    @Override
    public Map<String, String> resetAdminPassword(AdminResetPasswordRequest request) {
        String email = request.getEmail();
        try {
            adminPasswordResetService.resetPassword(email, request.getOtp(), request.getNewPassword());
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            String userId = userOpt.map(UserEntity::getId).orElse(null);
            auditService.logPasswordResetEvent(userId, email, true, "Password updated via admin reset");
            return Map.of("message", "Password updated successfully");
        } catch (IllegalArgumentException e) {
            auditService.logPasswordResetEvent(null, email, false, "Password reset failed: " + e.getMessage());
            return Map.of("status", String.valueOf(HttpStatus.BAD_REQUEST.value()), "message", e.getMessage());
        } catch (UsernameNotFoundException e) {
            return Map.of("status", String.valueOf(HttpStatus.NOT_FOUND.value()), "message", e.getMessage());
        } catch (Exception e) {
            auditService.logPasswordResetEvent(null, email, false, "Password reset failed: " + e.getMessage());
            return Map.of("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "message", "Password reset failed");
        }
    }
}
