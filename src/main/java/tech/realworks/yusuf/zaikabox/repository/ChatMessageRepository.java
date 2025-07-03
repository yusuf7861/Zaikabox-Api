package tech.realworks.yusuf.zaikabox.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tech.realworks.yusuf.zaikabox.entity.ChatMessageEntity;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessageEntity, String> {
    List<ChatMessageEntity> findBySessionIdOrderByTimestampAsc(String sessionId);
    List<ChatMessageEntity> findByUserIdOrderByTimestampDesc(String userId);
    List<ChatMessageEntity> findByIntent(String intent);
}
