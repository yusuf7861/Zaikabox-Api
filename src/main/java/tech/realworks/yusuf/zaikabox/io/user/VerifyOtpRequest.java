package tech.realworks.yusuf.zaikabox.io.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be a 6-digit code")
    private String otp;
    @NotBlank(message = "Token is required")
    private String token;
}
