package org.example.promotion.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.context.annotation.Bean;
import java.util.Optional;

/**
 * Enables JPA Auditing and provides a simple AuditorAware implementation.
 * In a real application this would extract the current user from the security
 * context.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditAwareConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // Placeholder implementation – returns a constant system user.
        // Replace with security context lookup when JWT authentication is added.
        return () -> Optional.of("system");
    }
}
