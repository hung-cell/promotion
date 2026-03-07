package org.example.promotion.engine.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.promotion.engine.entity.Promotion;
import org.example.promotion.engine.repository.PromotionRepository;
import org.example.promotion.common.enums.PromotionStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Service responsible for managing promotion data in Redis cache.
 * On startup it warms up the cache by loading all ACTIVE promotions
 * from the management database (same local MySQL instance).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionCacheService {

    private static final String PROMOTION_KEY_PREFIX = "promotion:";
    private static final String ACTIVE_PROMOTIONS_KEY = "promotions:active";
    private static final String QUOTA_KEY_PREFIX = "promotion:quota:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final PromotionRepository promotionRepository;

    @Value("${cache.promotion-ttl:300}")
    private long promotionTtlSeconds;

    // ─── Warm-up on startup ──────────────────────────────────────────────────

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCache() {
        log.info("=== [Cache Warm-up] Loading active promotions into Redis... ===");
        long start = System.currentTimeMillis();

        try {
            List<Promotion> activePromotions = promotionRepository
                    .findAllWithFilters(PromotionStatus.ACTIVE,
                            org.springframework.data.domain.PageRequest.of(0, 1000))
                    .getContent();

            int loaded = 0;
            for (Promotion promotion : activePromotions) {
                // Only cache promotions that haven't expired yet
                if (promotion.getEndDate().isAfter(LocalDateTime.now())) {
                    cachePromotion(promotion);
                    initializeQuotaCounter(promotion);
                    loaded++;
                }
            }

            long elapsed = System.currentTimeMillis() - start;
            log.info("=== [Cache Warm-up] DONE. Loaded {} promotions in {}ms ===", loaded, elapsed);
        } catch (Exception e) {
            log.error("[Cache Warm-up] Failed to warm up cache: {}", e.getMessage(), e);
            // Don't crash startup — service will fall back to DB on cache miss
        }
    }

    // ─── Cache Operations ─────────────────────────────────────────────────────

    public void cachePromotion(Promotion promotion) {
        String key = PROMOTION_KEY_PREFIX + promotion.getId();
        redisTemplate.opsForValue().set(key, promotion, promotionTtlSeconds, TimeUnit.SECONDS);
        // Add ID to active set
        redisTemplate.opsForSet().add(ACTIVE_PROMOTIONS_KEY, promotion.getId().toString());
    }

    public Optional<Promotion> getPromotion(Long promotionId) {
        String key = PROMOTION_KEY_PREFIX + promotionId;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof Promotion p) {
            return Optional.of(p);
        }
        // Cache miss: fallback to DB
        log.debug("[Cache] Miss for promotionId={}, fetching from DB", promotionId);
        Optional<Promotion> fromDb = promotionRepository.findByIdAndIsDeletedFalse(promotionId);
        fromDb.ifPresent(this::cachePromotion);
        return fromDb;
    }

    /**
     * Returns all currently active & unexpired promotions from cache.
     * Falls back to DB if cache is empty.
     */
    public List<Promotion> getActivePromotions() {
        var activeIds = redisTemplate.opsForSet().members(ACTIVE_PROMOTIONS_KEY);
        if (activeIds == null || activeIds.isEmpty()) {
            log.warn("[Cache] Active promotions set is empty, falling back to DB");
            return promotionRepository
                    .findAllWithFilters(PromotionStatus.ACTIVE,
                            org.springframework.data.domain.PageRequest.of(0, 1000))
                    .getContent();
        }

        List<Promotion> result = new ArrayList<>();
        for (Object idObj : activeIds) {
            try {
                Long id = Long.parseLong(idObj.toString());
                getPromotion(id).ifPresent(result::add);
            } catch (NumberFormatException e) {
                log.warn("[Cache] Skipping invalid promotion ID in set: {}", idObj);
            }
        }
        return result;
    }

    public void invalidate(Long promotionId) {
        log.info("[Cache] Invalidating promotionId={}", promotionId);
        redisTemplate.delete(PROMOTION_KEY_PREFIX + promotionId);
        redisTemplate.opsForSet().remove(ACTIVE_PROMOTIONS_KEY, promotionId.toString());
        redisTemplate.delete(QUOTA_KEY_PREFIX + promotionId);
    }

    public void invalidateAll() {
        log.warn("[Cache] Invalidating ALL promotion cache");
        redisTemplate.delete(ACTIVE_PROMOTIONS_KEY);
    }

    // ─── Quota (Redis Atomic Counters) ────────────────────────────────────────

    private void initializeQuotaCounter(Promotion promotion) {
        if (promotion.getUsageLimit() != null) {
            String key = QUOTA_KEY_PREFIX + promotion.getId();
            // Available = limit - already used
            long available = promotion.getUsageLimit() - promotion.getUsageCount();
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                redisTemplate.opsForValue().set(key, available, promotionTtlSeconds, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * Atomically decrements the quota counter.
     * Returns true if reservation was successful, false if quota exhausted.
     */
    public boolean tryReserveQuota(Long promotionId) {
        String key = QUOTA_KEY_PREFIX + promotionId;
        Object val = redisTemplate.opsForValue().get(key);
        if (val == null) {
            // No quota limit configured → unlimited
            return true;
        }
        Long remaining = redisTemplate.opsForValue().decrement(key);
        if (remaining != null && remaining < 0) {
            // Over-decremented → roll back
            redisTemplate.opsForValue().increment(key);
            return false;
        }
        return true;
    }

    /**
     * Returns a quota slot (on release/rollback).
     */
    public void releaseQuota(Long promotionId) {
        String key = QUOTA_KEY_PREFIX + promotionId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().increment(key);
        }
    }

    public Long getRemainingQuota(Long promotionId) {
        String key = QUOTA_KEY_PREFIX + promotionId;
        Object val = redisTemplate.opsForValue().get(key);
        if (val == null)
            return null; // unlimited
        return Long.parseLong(val.toString());
    }
}
