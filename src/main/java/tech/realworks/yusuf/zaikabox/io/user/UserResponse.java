package tech.realworks.yusuf.zaikabox.io.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private String id;
    private String name;
    private String email;
}
