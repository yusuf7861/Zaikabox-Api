package tech.realworks.yusuf.zaikabox.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tech.realworks.yusuf.zaikabox.entity.RefreshTokenEntity;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshTokenEntity, String> {
    Optional<RefreshTokenEntity> findByToken(String token);
    void deleteByToken(String token);
    void deleteByUserId(String userId);
}
