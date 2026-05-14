package tech.realworks.yusuf.zaikabox.event;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderLifecycleEventListener {

    private final MeterRegistry meterRegistry;

    public OrderLifecycleEventListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Async
    @EventListener
    public void onOrderLifecycleEvent(OrderLifecycleEvent event) {
        log.info("Order lifecycle event source={} orderId={} customerId={} status={}",
                event.getSource(), event.getOrderId(), event.getCustomerId(), event.getStatus());

        Counter.builder("zaikabox.orders.lifecycle.events")
                .tag("status", event.getStatus())
                .tag("source", event.getSource())
                .register(meterRegistry)
                .increment();
    }
}
