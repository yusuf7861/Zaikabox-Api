package tech.realworks.yusuf.zaikabox.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tech.realworks.yusuf.zaikabox.entity.OrderEntity;
import tech.realworks.yusuf.zaikabox.entity.Status;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for OrderEntity.
 * Provides methods to interact with the orders collection in the MongoDB database.
 */
@Repository
public interface OrderRepository extends MongoRepository<OrderEntity, String> {
    
    /**
     * Find an order by its custom order ID
     * @param orderId The custom order ID
     * @return Optional containing the order if found
     */
    Optional<OrderEntity> findByOrderId(String orderId);
    
    /**
     * Find all orders for a specific customer
     * @param customerId The customer ID
     * @return List of orders for the customer
     */
    List<OrderEntity> findByCustomerId(String customerId);
    
    /**
     * Find all orders for a specific customer with a specific status
     * @param customerId The customer ID
     * @param status The order status
     * @return List of orders for the customer with the specified status
     */
    List<OrderEntity> findByCustomerIdAndStatus(String customerId, Status status);

    void deleteByOrderId(String orderId);
}