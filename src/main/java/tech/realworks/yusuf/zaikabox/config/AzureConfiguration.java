package tech.realworks.yusuf.zaikabox.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfiguration {
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
            System.out.println("Azure Blob init failed: " + e.getMessage());
            return null;
        }
    }
}
