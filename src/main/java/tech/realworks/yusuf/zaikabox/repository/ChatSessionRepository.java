package tech.realworks.yusuf.zaikabox.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tech.realworks.yusuf.zaikabox.entity.ChatSessionEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatSessionRepository extends MongoRepository<ChatSessionEntity, String> {
    List<ChatSessionEntity> findByUserIdOrderByStartTimeDesc(String userId);
    List<ChatSessionEntity> findByStatusAndLastActivityTimeBefore(ChatSessionEntity.SessionStatus status, LocalDateTime time);
    ChatSessionEntity findTopByUserIdOrderByStartTimeDesc(String userId);
}
