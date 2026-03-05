package org.example.promotion.engine.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.promotion.engine.cache.PromotionCacheService;
import org.example.promotion.engine.entity.Promotion;
import org.example.promotion.engine.repository.PromotionRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Listens to Kafka events published by promotion-management-service.
 * Keeps Redis cache up-to-date in real time whenever a promotion changes.
 *
 * Expected message format (JSON):
 * { "action": "ACTIVATE|DISABLE|UPDATE|DELETE", "promotionId": 123 }
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionEventConsumer {

    private final PromotionCacheService cacheService;
    private final PromotionRepository promotionRepository;

    @KafkaListener(topics = "${kafka.topics.promotion-lifecycle:promotion.lifecycle.events}", groupId = "${spring.kafka.consumer.group-id:promotion-engine-consumer}", containerFactory = "kafkaListenerContainerFactory")
    public void handlePromotionEvent(Map<String, Object> event) {
        if (event == null)
            return;

        String action = String.valueOf(event.get("action"));
        Object idObj = event.get("promotionId");
        if (idObj == null) {
            log.warn("[Kafka] Event missing promotionId: {}", event);
            return;
        }

        Long promotionId = Long.parseLong(idObj.toString());
        log.info("[Kafka] Received event: action={}, promotionId={}", action, promotionId);

        switch (action) {
            case "CREATE", "UPDATE", "ACTIVATE" -> refreshCache(promotionId);
            case "DISABLE", "DELETE" -> cacheService.invalidate(promotionId);
            default -> log.warn("[Kafka] Unknown action: {}", action);
        }
    }

    private void refreshCache(Long promotionId) {
        promotionRepository.findByIdAndIsDeletedFalse(promotionId).ifPresentOrElse(
                promotion -> {
                    cacheService.cachePromotion(promotion);
                    log.info("[Kafka] Cache refreshed for promotionId={}", promotionId);
                },
                () -> log.warn("[Kafka] Promotion not found in DB: promotionId={}", promotionId));
    }
}
