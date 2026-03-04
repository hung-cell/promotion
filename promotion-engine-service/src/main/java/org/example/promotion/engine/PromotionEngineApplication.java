package org.example.promotion.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "org.example.promotion.engine",
        "org.example.promotion.common"
})
@EnableCaching
@EnableAsync
@EnableScheduling
public class PromotionEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromotionEngineApplication.class, args);
    }

}
