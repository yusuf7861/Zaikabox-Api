package tech.realworks.yusuf.zaikabox.io.user;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String otp;
    private String token;
}
