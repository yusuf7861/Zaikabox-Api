package tech.realworks.yusuf.zaikabox.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tech.realworks.yusuf.zaikabox.entity.PaymentRequestEntity;

import java.util.Optional;

@Repository
public interface PaymentRequestRepository extends MongoRepository<PaymentRequestEntity, String> {
    Optional<PaymentRequestEntity> findByRazorpayOrderId(String razorpayOrderId);
}
