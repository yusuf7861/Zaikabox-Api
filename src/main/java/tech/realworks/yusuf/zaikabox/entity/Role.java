package tech.realworks.yusuf.zaikabox.entity;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collections;
import java.util.List;

public enum Role {
    ADMIN, CUSTOMER;

    public List<GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.name()));
    }

    public static class SimpleGrantedAuthority implements GrantedAuthority {
        private final String authority;

        public SimpleGrantedAuthority(String authority) {
            this.authority = authority;
        }

        @Override
        public String getAuthority() {
            return authority;
        }

        @Override
        public String toString() {
            return authority;
        }
    }
}
