package tech.realworks.yusuf.zaikabox.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    @Override
    public void changeStatusOfOrder(String orderId, Status status) {
        Optional<OrderEntity> byOrderId = orderRepository.findByOrderId(orderId);
        if (byOrderId.isEmpty()) {
            log.error("Order not found: {}", orderId);
            return;
        }

        OrderEntity orderEntity = byOrderId.get();
        orderEntity.setStatus(status);
        orderRepository.save(orderEntity);
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
            orderRepository.deleteByOrderId(orderId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
