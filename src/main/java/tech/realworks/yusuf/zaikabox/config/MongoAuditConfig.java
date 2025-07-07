package tech.realworks.yusuf.zaikabox.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class MongoAuditConfig {

    private final MongoTemplate mongoTemplate;
    private final MongoMappingContext mongoMappingContext;

    @Bean
    CommandLineRunner commandLineRunner() {
        return args -> {
            // Create the collection if it doesn't exist
            if (!mongoTemplate.collectionExists(AuditEntity.class)) {
                mongoTemplate.createCollection(AuditEntity.class);
                log.info("Created audit_logs collection");
            } else {
                log.info("audit_logs collection already exists");
            }

            // Create indexes for AuditEntity
            IndexOperations indexOps = mongoTemplate.indexOps(AuditEntity.class);
            IndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);
            resolver.resolveIndexFor(AuditEntity.class).forEach(indexOps::ensureIndex);
        };
    }
}
