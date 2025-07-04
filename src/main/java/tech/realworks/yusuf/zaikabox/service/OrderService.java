package tech.realworks.yusuf.zaikabox.service;

import tech.realworks.yusuf.zaikabox.entity.OrderEntity;
import tech.realworks.yusuf.zaikabox.entity.Status;

public interface OrderService {
    void changeStatusOfOrder(String orderId, Status status);
    Status getStatusOfOrder(String orderId);
    void deleteOrder(String orderId);
}
