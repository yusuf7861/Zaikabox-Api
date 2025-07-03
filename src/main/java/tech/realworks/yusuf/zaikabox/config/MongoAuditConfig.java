package tech.realworks.yusuf.zaikabox.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import tech.realworks.yusuf.zaikabox.entity.AuditEntity;

@Configuration
@RequiredArgsConstructor
public class MongoAuditConfig {

    private final MongoTemplate mongoTemplate;
    private final MongoMappingContext mongoMappingContext;

    @Bean
    CommandLineRunner commandLineRunner() {
        return args -> {
            if (!mongoTemplate.collectionExists(AuditEntity.class)) {
                mongoTemplate.createCollection(AuditEntity.class);
                System.out.println("Created audit_logs collection");
            } else {
                System.out.println("audit_logs collection already exists");
            }

            IndexOperations indexOps = mongoTemplate.indexOps(AuditEntity.class);
            IndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);
            resolver.resolveIndexFor(AuditEntity.class).forEach(indexOps::ensureIndex);
        };
    }
}
