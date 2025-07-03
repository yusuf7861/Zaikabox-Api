package tech.realworks.yusuf.zaikabox.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tech.realworks.yusuf.zaikabox.entity.DietaryPreferenceEntity;

import java.util.Optional;

public interface DietaryPreferenceRepository extends MongoRepository<DietaryPreferenceEntity, String> {
    Optional<DietaryPreferenceEntity> findByUserId(String userId);
}
