package tech.realworks.yusuf.zaikabox.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import tech.realworks.yusuf.zaikabox.entity.FAQEntity;

import java.util.List;

public interface FAQRepository extends MongoRepository<FAQEntity, String> {
    List<FAQEntity> findByKeywordsContainingAndIsActiveTrue(String keyword);
    List<FAQEntity> findByCategoryAndIsActiveTrue(String category);

    @Query("{'question': {$regex: ?0, $options: 'i'}, 'isActive': true}")
    List<FAQEntity> findByQuestionContainingIgnoreCaseAndIsActiveTrue(String questionFragment);

    List<FAQEntity> findByIsActiveTrueOrderByPopularityDesc();
}
