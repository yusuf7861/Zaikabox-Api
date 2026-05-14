package tech.realworks.yusuf.zaikabox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ZaikaboxApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZaikaboxApiApplication.class, args);
    }

}
