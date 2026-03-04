package org.example.promotion.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.promotion.enums.PromotionStatus;
import org.example.promotion.repository.PromotionRepository;
import org.example.promotion.service.IAuditLogService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionScheduler {

    private final PromotionRepository promotionRepository;
    private final IAuditLogService auditLogService;

    @Scheduled(fixedRateString = "${promotion.scheduler.expire-rate:60000}")
    @Transactional
    public void expirePromotions() {
        log.info("Running scheduled job to expire promotions at {}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();
        int updatedCount = promotionRepository.expirePromotions(now, PromotionStatus.ACTIVE, PromotionStatus.EXPIRED);

        if (updatedCount > 0) {
            log.info("Expired {} promotions", updatedCount);
            // We could consider adding audit logs here, but since it's a bulk query,
            // updating audit logs line by line might not be perfectly efficient.
            // A more complex system might emit an event and handle it async.
        }
    }
}
