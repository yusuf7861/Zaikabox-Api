package tech.realworks.yusuf.zaikabox.service.userservice;

import org.springframework.security.core.Authentication;

public interface AuthenticationFacade {
    Authentication getAuthentication();
}
