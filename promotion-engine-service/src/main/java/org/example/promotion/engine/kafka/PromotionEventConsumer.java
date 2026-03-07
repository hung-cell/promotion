package org.example.promotion.engine.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.promotion.engine.cache.PromotionCacheService;
import org.example.promotion.engine.entity.Promotion;
import org.example.promotion.engine.repository.PromotionRepository;
import org.example.promotion.common.enums.PromotionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.promotion-lifecycle:promotion.lifecycle.events}", groupId = "${spring.kafka.consumer.group-id:promotion-engine-consumer}", containerFactory = "kafkaListenerContainerFactory")
    public void handlePromotionEvent(String eventPayload) {
        if (eventPayload == null)
            return;

        Map<String, Object> event;
        try {
            event = objectMapper.readValue(eventPayload, Map.class);
        } catch (Exception e) {
            log.error("[Kafka] Failed to parse event payload", e);
            return;
        }

        String action = String.valueOf(event.get("action"));
        Object idObj = event.get("promotionId");
        if (idObj == null) {
            log.warn("[Kafka] Event missing promotionId: {}", event);
            return;
        }

        Long promotionId = Long.parseLong(idObj.toString());
        log.info("[Kafka] Received event: action={}, promotionId={}", action, promotionId);

        switch (action) {
            case "CREATE", "UPDATE", "ACTIVATE" -> refreshCache(promotionId, event);
            case "DISABLE", "DELETE" -> {
                promotionRepository.findById(promotionId).ifPresent(p -> {
                    p.setStatus(PromotionStatus.DISABLED);
                    promotionRepository.save(p);
                });
                cacheService.invalidate(promotionId);
            }
            default -> log.warn("[Kafka] Unknown action: {}", action);
        }
    }

    private void refreshCache(Long promotionId, Map<String, Object> event) {
        Object promoData = event.get("promotion");
        if (promoData != null) {
            try {
                Promotion parsedPromotion = objectMapper.convertValue(promoData, Promotion.class);

                // Fix bidirectional mapping for cascading saves
                if (parsedPromotion.getRules() != null) {
                    parsedPromotion.getRules().forEach(r -> r.setPromotion(parsedPromotion));
                }
                if (parsedPromotion.getConditions() != null) {
                    parsedPromotion.getConditions().forEach(c -> c.setPromotion(parsedPromotion));
                }

                Promotion savedPromotion = promotionRepository.save(parsedPromotion);
                cacheService.cachePromotion(savedPromotion);
                log.info("[Kafka] Synced promotion from event data: id={}", promotionId);
            } catch (Exception e) {
                log.error("[Kafka] Failed to parse and sync promotion data for id={}", promotionId, e);
            }
        } else {
            log.warn("[Kafka] Event for CREATE/UPDATE/ACTIVATE missing 'promotion' payload! id={}", promotionId);
        }
    }
}
