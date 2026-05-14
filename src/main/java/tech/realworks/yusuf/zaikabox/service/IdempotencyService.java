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
        try {
            return (T) cache.asMap().computeIfAbsent(key, ignored -> {
                try {
                    return task.call();
                } catch (Exception e) {
                    throw new TaskExecutionRuntimeException(e);
                }
            });
        } catch (TaskExecutionRuntimeException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    private static class TaskExecutionRuntimeException extends RuntimeException {
        TaskExecutionRuntimeException(Throwable cause) {
            super(cause);
        }
    }
}
