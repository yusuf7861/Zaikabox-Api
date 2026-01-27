package tech.realworks.yusuf.zaikabox.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.realworks.yusuf.zaikabox.io.user.AdminResetPasswordRequest;
import tech.realworks.yusuf.zaikabox.io.user.SendResetOtpRequest;
import tech.realworks.yusuf.zaikabox.io.user.UserResponse;
import tech.realworks.yusuf.zaikabox.io.user.VerifyAdminOtpRequest;
import tech.realworks.yusuf.zaikabox.service.userservice.AdminUserService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin User Management", description = "Admin-only APIs for managing users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "Get all users (admin only)", description = "Retrieves all users. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Delete current user (admin only)", description = "Deletes the currently authenticated user. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "User deleted successfully")
    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteUser() {
        Map<String, String> result = adminUserService.deleteCurrentUser();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Send admin password reset OTP", description = "Generates a 6-digit OTP for admin accounts and emails it.")
    @ApiResponse(responseCode = "200", description = "OTP sent successfully")
    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendAdminPasswordResetOtp(@Valid @RequestBody SendResetOtpRequest request) {
        Map<String, String> result = adminUserService.sendAdminPasswordResetOtp(request);
        HttpStatus status = parseStatus(result);
        return new ResponseEntity<>(clean(result), status);
    }

    @Operation(summary = "Verify admin password reset OTP", description = "Verifies the OTP for admin password reset.")
    @ApiResponse(responseCode = "200", description = "OTP verified successfully")
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyAdminOtp(@Valid @RequestBody VerifyAdminOtpRequest request) {
        Map<String, String> result = adminUserService.verifyAdminOtp(request);
        HttpStatus status = parseStatus(result);
        return new ResponseEntity<>(clean(result), status);
    }

    @Operation(summary = "Reset admin password", description = "Resets the admin user's password after OTP verification.")
    @ApiResponse(responseCode = "200", description = "Password updated successfully")
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetAdminPassword(@Valid @RequestBody AdminResetPasswordRequest request) {
        Map<String, String> result = adminUserService.resetAdminPassword(request);
        HttpStatus status = parseStatus(result);
        return new ResponseEntity<>(clean(result), status);
    }

    // Helper to parse optional status from service Map
    private HttpStatus parseStatus(Map<String, String> result) {
        try {
            String code = result.get("status");
            return code != null ? HttpStatus.valueOf(Integer.parseInt(code)) : HttpStatus.OK;
        } catch (Exception ignored) {
            return HttpStatus.OK;
        }
    }

    // Helper to remove status key from body
    private Map<String, String> clean(Map<String, String> result) {
        if (result.containsKey("status")) {
            result = new java.util.HashMap<>(result);
            result.remove("status");
        }
        return result;
    }
}
