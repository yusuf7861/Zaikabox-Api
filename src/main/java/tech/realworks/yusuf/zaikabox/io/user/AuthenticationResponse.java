package tech.realworks.yusuf.zaikabox.io.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticationResponse {
    private String token;
    private String email;
}
