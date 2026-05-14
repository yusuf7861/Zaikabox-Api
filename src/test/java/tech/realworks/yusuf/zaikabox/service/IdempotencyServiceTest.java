package tech.realworks.yusuf.zaikabox.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdempotencyServiceTest {

    private final IdempotencyService idempotencyService = new IdempotencyService();

    @Test
    void shouldReuseCachedResultForSameIdempotencyKey() {
        AtomicInteger counter = new AtomicInteger(0);

        String first = idempotencyService.execute("same-key", "op", () -> "value-" + counter.incrementAndGet());
        String second = idempotencyService.execute("same-key", "op", () -> "value-" + counter.incrementAndGet());

        assertEquals("value-1", first);
        assertEquals("value-1", second);
        assertEquals(1, counter.get());
    }
}
