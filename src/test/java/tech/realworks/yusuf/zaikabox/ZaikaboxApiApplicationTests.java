package tech.realworks.yusuf.zaikabox;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        "AZURE_CONNECTION_STRING=UseDevelopmentStorage=true",
        "AZURE_CONTAINER_NAME=zaikabox-images",
        "JWT_SECRET=c3VwZXJzZWNyZXQxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=",
        "MAIL_HOST=smtp.gmail.com",
        "MAIL_USERNAME=no-reply@example.com",
        "MAIL_PASSWORD=dummy",
        "RAZORPAY_KEY=rzp_test_key",
        "RAZORPAY_SECRET=rzp_test_secret",
        "GEMINI_API_KEY=dummy",
        "app.admin.initializer.enabled=false"
})
@Testcontainers(disabledWithoutDocker = true)
class ZaikaboxApiApplicationTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_URI", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void contextLoads() {
    }

}
