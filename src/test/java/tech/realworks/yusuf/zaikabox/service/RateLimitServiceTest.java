package tech.realworks.yusuf.zaikabox.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitServiceTest {

    private final RateLimitService rateLimitService = new RateLimitService();

    @Test
    void shouldBlockAfterLimitIsReached() {
        String key = "127.0.0.1:/api/v1/auth/login";
        for (int i = 0; i < 30; i++) {
            assertTrue(rateLimitService.allow(key));
        }
        assertFalse(rateLimitService.allow(key));
    }
}
