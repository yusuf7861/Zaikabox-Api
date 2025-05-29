package tech.realworks.yusuf.zaikabox.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tech.realworks.yusuf.zaikabox.entity.CartEntity;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<CartEntity, String> {

    Optional<CartEntity> findByUserId(String userId);
    void deleteByUserId(String userId);
}
