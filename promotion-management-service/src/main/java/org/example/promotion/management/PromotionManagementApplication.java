package org.example.promotion.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "org.example.promotion.management",
        "org.example.promotion.common"
})
@EnableScheduling
public class PromotionManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromotionManagementApplication.class, args);
    }

}
