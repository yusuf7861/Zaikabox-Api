package tech.realworks.yusuf.zaikabox.event;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class OrderLifecycleEvent {
    String orderId;
    String customerId;
    String status;
    String source;
    LocalDateTime occurredAt;
}
