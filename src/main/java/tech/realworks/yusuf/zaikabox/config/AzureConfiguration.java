package tech.realworks.yusuf.zaikabox.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureConfiguration.class);

    @Value("${spring.cloud.azure.storage.connection-string}")
    private String connectionString;
    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerName;

    @Bean
    public BlobContainerClient blobContainerClient() {
        try {
            return new BlobContainerClientBuilder()
                    .connectionString(connectionString)
                    .containerName(containerName)
                    .buildClient();
        } catch (Exception e) {
            LOGGER.error("Azure Blob init failed: {}", e.getMessage(), e);
            return null;
        }
    }
}
