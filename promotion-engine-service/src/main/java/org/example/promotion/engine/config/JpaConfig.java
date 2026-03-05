package org.example.promotion.engine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // Enables @CreatedDate / @LastModifiedDate auto-fill for BaseEntity
}
