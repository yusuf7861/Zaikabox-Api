package tech.realworks.yusuf.zaikabox.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private static final int MAX_REQUESTS_PER_MINUTE = 30;
    private final Cache<String, CounterWindow> requestCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(1))
            .maximumSize(20_000)
            .build();

    public boolean allow(String key) {
        CounterWindow counterWindow = requestCache.get(key, ignored -> new CounterWindow());
        synchronized (counterWindow) {
            if (counterWindow.count >= MAX_REQUESTS_PER_MINUTE) {
                return false;
            }
            counterWindow.count++;
            return true;
        }
    }

    private static class CounterWindow {
        private int count = 0;
    }
}
