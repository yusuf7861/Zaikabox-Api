package tech.realworks.yusuf.zaikabox.io.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {

    private String id; // for testing purpose
    private String name;
    private String email;
    private String password;
}
