package tech.realworks.yusuf.zaikabox.service.userservice;

import tech.realworks.yusuf.zaikabox.io.user.AdminResetPasswordRequest;
import tech.realworks.yusuf.zaikabox.io.user.SendResetOtpRequest;
import tech.realworks.yusuf.zaikabox.io.user.UserResponse;
import tech.realworks.yusuf.zaikabox.io.user.VerifyAdminOtpRequest;

import java.util.List;
import java.util.Map;

public interface AdminUserService {
    List<UserResponse> getAllUsers();
    Map<String, String> deleteCurrentUser();
    Map<String, String> sendAdminPasswordResetOtp(SendResetOtpRequest request);
    Map<String, String> verifyAdminOtp(VerifyAdminOtpRequest request);
    Map<String, String> resetAdminPassword(AdminResetPasswordRequest request);
}
