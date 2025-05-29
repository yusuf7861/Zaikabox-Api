package tech.realworks.yusuf.zaikabox.service.userService;

import org.springframework.security.core.Authentication;

public interface AuthenticationFacade {
    Authentication getAuthentication();
}
