package tech.realworks.yusuf.zaikabox.io.user;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String token;
    private String password;
}
