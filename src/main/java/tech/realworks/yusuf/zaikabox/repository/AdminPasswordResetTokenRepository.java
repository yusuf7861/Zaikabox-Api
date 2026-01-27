package tech.realworks.yusuf.zaikabox.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tech.realworks.yusuf.zaikabox.entity.AdminPasswordResetToken;

import java.util.Optional;

public interface AdminPasswordResetTokenRepository extends MongoRepository<AdminPasswordResetToken, String> {
    Optional<AdminPasswordResetToken> findFirstByEmailOrderByCreatedAtDesc(String email);

    void deleteByEmail(String email);
}
