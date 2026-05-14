package tech.realworks.yusuf.zaikabox.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.Callable;

@Service
public class IdempotencyService {

    private final Cache<String, Object> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(15))
            .maximumSize(10_000)
            .build();

    @SuppressWarnings("unchecked")
    public <T> T execute(String idempotencyKey, String operation, Callable<T> task) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        String key = operation + ":" + idempotencyKey;
        Object existing = cache.getIfPresent(key);
        if (existing != null) {
            return (T) existing;
        }

        synchronized (key.intern()) {
            existing = cache.getIfPresent(key);
            if (existing != null) {
                return (T) existing;
            }
            try {
                T result = task.call();
                cache.put(key, result);
                return result;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
