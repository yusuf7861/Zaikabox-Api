package tech.realworks.yusuf.zaikabox.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tech.realworks.yusuf.zaikabox.event.OrderLifecycleEvent;
import tech.realworks.yusuf.zaikabox.entity.OrderEntity;
import tech.realworks.yusuf.zaikabox.entity.Status;
import tech.realworks.yusuf.zaikabox.repository.OrderRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderImpl implements OrderService{

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void changeStatusOfOrder(String orderId, Status status) {
        Optional<OrderEntity> byOrderId = orderRepository.findByOrderId(orderId);
        if (byOrderId.isEmpty()) {
            log.error("Order not found: {}", orderId);
            return;
        }

        OrderEntity orderEntity = byOrderId.get();
        orderEntity.setStatus(status);
        OrderEntity saved = orderRepository.save(orderEntity);
        eventPublisher.publishEvent(OrderLifecycleEvent.builder()
                .orderId(saved.getOrderId())
                .customerId(saved.getCustomerId())
                .status(status.name())
                .source("admin_order_status_update")
                .occurredAt(java.time.LocalDateTime.now())
                .build());
    }

    @Override
    public Status getStatusOfOrder(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .map(OrderEntity::getStatus)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
    }

    @Override
    public void deleteOrder(String orderId) {
        try {
            Optional<OrderEntity> byOrderId = orderRepository.findByOrderId(orderId);
            if (byOrderId.isEmpty()) {
                log.error("Order not found with ID: {}", orderId);
                return;
            }
            OrderEntity existing = byOrderId.get();
            orderRepository.deleteByOrderId(orderId);
            eventPublisher.publishEvent(OrderLifecycleEvent.builder()
                    .orderId(existing.getOrderId())
                    .customerId(existing.getCustomerId())
                    .status(Status.CANCELLED.name())
                    .source("admin_order_delete")
                    .occurredAt(java.time.LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
