package tech.realworks.yusuf.zaikabox.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "Zaikabox API",
        description = "API documentation for Zaikabox",
        version = "1.0.0"
    )
)
@Configuration
public class OpenApiConfig {

}
