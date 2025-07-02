package tech.realworks.yusuf.zaikabox.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tech.realworks.yusuf.zaikabox.entity.AuditEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditRepository extends MongoRepository<AuditEntity, String> {
    List<AuditEntity> findByUserId(String userId);
    List<AuditEntity> findByEventType(String eventType);
    List<AuditEntity> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<AuditEntity> findByEmail(String email);
}
