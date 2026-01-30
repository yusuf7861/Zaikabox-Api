package tech.realworks.yusuf.zaikabox.io.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.realworks.yusuf.zaikabox.entity.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminProfileResponse {
    private String name;
    private String email;
    private Role role;
}
