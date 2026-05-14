package tech.realworks.yusuf.zaikabox.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {

    private static final int MAX_REQUESTS_PER_MINUTE = 30;
    private final Cache<String, AtomicInteger> requestCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(1))
            .maximumSize(20_000)
            .build();

    public boolean allow(String key) {
        AtomicInteger counter = requestCache.get(key, ignored -> new AtomicInteger(0));
        return counter.incrementAndGet() <= MAX_REQUESTS_PER_MINUTE;
    }
}
