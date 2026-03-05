package org.example.promotion.engine.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.promotion.common.enums.ConditionType;
import org.example.promotion.common.enums.PromotionType;
import org.example.promotion.engine.dto.request.CalculateRequest;
import org.example.promotion.engine.dto.request.CalculateRequest.CartItem;
import org.example.promotion.engine.dto.response.CalculateResponse;
import org.example.promotion.engine.dto.response.CalculateResponse.AppliedPromotion;
import org.example.promotion.engine.dto.response.CalculateResponse.CalculationSummary;
import org.example.promotion.engine.dto.response.CalculateResponse.ItemDiscount;
import org.example.promotion.engine.entity.Promotion;
import org.example.promotion.engine.entity.PromotionCondition;
import org.example.promotion.engine.entity.PromotionRule;
import org.example.promotion.engine.cache.PromotionCacheService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Core service that calculates which promotions apply to a cart
 * and computes the final discount amounts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionCalculationService {

    private final PromotionCacheService cacheService;
    private final ObjectMapper objectMapper;

    public CalculateResponse calculate(CalculateRequest request) {
        log.info("[Calculate] customerId={}, subtotal={}, items={}", request.getCustomerId(), request.getSubtotal(),
                request.getItems().size());

        List<Promotion> allActive = cacheService.getActivePromotions();
        List<AppliedPromotion> applied = new ArrayList<>();
        BigDecimal shippingDiscount = BigDecimal.ZERO;

        for (Promotion promotion : allActive) {
            if (!isActiveAndValid(promotion))
                continue;
            if (!meetsConditions(promotion, request))
                continue;

            AppliedPromotion result = applyPromotion(promotion, request);
            if (result != null) {
                applied.add(result);
                if (PromotionType.FREE_SHIPPING.equals(promotion.getType())) {
                    shippingDiscount = shippingDiscount.add(result.getDiscountAmount());
                }
            }
        }

        // Build summary
        BigDecimal totalDiscount = applied.stream()
                .map(AppliedPromotion::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(shippingDiscount);

        BigDecimal originalShipping = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;
        BigDecimal finalShipping = originalShipping.subtract(shippingDiscount).max(BigDecimal.ZERO);
        BigDecimal finalSubtotal = request.getSubtotal().subtract(totalDiscount).max(BigDecimal.ZERO);
        BigDecimal savedAmount = totalDiscount.add(shippingDiscount);
        double savedPct = request.getSubtotal().compareTo(BigDecimal.ZERO) > 0
                ? savedAmount.multiply(BigDecimal.valueOf(100)).divide(request.getSubtotal(), 2, RoundingMode.HALF_UP)
                        .doubleValue()
                : 0.0;

        List<String> messages = savedAmount.compareTo(BigDecimal.ZERO) > 0
                ? List.of(String.format("Bạn đã tiết kiệm được %,.0fđ với các ưu đãi!", savedAmount))
                : List.of("Không có ưu đãi nào áp dụng cho đơn hàng này");

        CalculationSummary summary = CalculationSummary.builder()
                .originalSubtotal(request.getSubtotal())
                .totalDiscount(totalDiscount)
                .finalSubtotal(finalSubtotal)
                .originalShippingFee(originalShipping)
                .shippingDiscount(shippingDiscount)
                .finalShippingFee(finalShipping)
                .totalAmount(finalSubtotal.add(finalShipping))
                .savedAmount(savedAmount)
                .savedPercentage(savedPct)
                .build();

        return CalculateResponse.builder()
                .calculationId("calc_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12))
                .timestamp(LocalDateTime.now())
                .applicablePromotions(applied)
                .summary(summary)
                .messages(messages)
                .build();
    }

    // ─── Promotion Eligibility ────────────────────────────────────────────────

    private boolean isActiveAndValid(Promotion promotion) {
        LocalDateTime now = LocalDateTime.now();
        return promotion.getStartDate().isBefore(now)
                && promotion.getEndDate().isAfter(now)
                && (promotion.getUsageLimit() == null
                        || promotion.getUsageCount() < promotion.getUsageLimit());
    }

    private boolean meetsConditions(Promotion promotion, CalculateRequest request) {
        if (promotion.getConditions() == null || promotion.getConditions().isEmpty())
            return true;

        for (PromotionCondition condition : promotion.getConditions()) {
            if (!evaluateCondition(condition, request))
                return false;
        }
        return true;
    }

    private boolean evaluateCondition(PromotionCondition condition, CalculateRequest request) {
        try {
            Map<String, Object> values = objectMapper.readValue(
                    condition.getConditionValue(), new TypeReference<>() {
                    });

            return switch (condition.getConditionType()) {
                case ORDER -> evaluateOrderCondition(values, request);
                case CUSTOMER_SEGMENT -> evaluateCustomerCondition(values, request);
                case PRODUCT -> true; // Product-level conditions handled during rule application
                case CHANNEL -> evaluateChannelCondition(values, request);
            };
        } catch (Exception e) {
            log.warn("[Condition] Could not parse conditionValue: {}", e.getMessage());
            return true; // Lenient: don't block on parse failure
        }
    }

    private boolean evaluateOrderCondition(Map<String, Object> values, CalculateRequest request) {
        if (values.containsKey("minOrderValue")) {
            BigDecimal min = new BigDecimal(values.get("minOrderValue").toString());
            if (request.getSubtotal().compareTo(min) < 0)
                return false;
        }
        return true;
    }

    private boolean evaluateCustomerCondition(Map<String, Object> values, CalculateRequest request) {
        if (values.containsKey("customerSegments") && request.getCustomerSegment() != null) {
            @SuppressWarnings("unchecked")
            List<String> segments = (List<String>) values.get("customerSegments");
            if (!segments.isEmpty() && !segments.contains(request.getCustomerSegment()))
                return false;
        }
        if (values.containsKey("customerTiers") && request.getCustomerTier() != null) {
            @SuppressWarnings("unchecked")
            List<String> tiers = (List<String>) values.get("customerTiers");
            if (!tiers.isEmpty() && !tiers.contains(request.getCustomerTier()))
                return false;
        }
        return true;
    }

    private boolean evaluateChannelCondition(Map<String, Object> values, CalculateRequest request) {
        if (values.containsKey("includeChannels") && request.getChannel() != null) {
            @SuppressWarnings("unchecked")
            List<String> channels = (List<String>) values.get("includeChannels");
            if (!channels.isEmpty() && !channels.contains(request.getChannel()))
                return false;
        }
        return true;
    }

    // ─── Rule Application ─────────────────────────────────────────────────────

    private AppliedPromotion applyPromotion(Promotion promotion, CalculateRequest request) {
        if (promotion.getRules() == null || promotion.getRules().isEmpty())
            return null;

        List<ItemDiscount> itemDiscounts = new ArrayList<>();
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (PromotionRule rule : promotion.getRules()) {
            BigDecimal discount = applyRule(rule, request, itemDiscounts);
            totalDiscount = totalDiscount.add(discount);
        }

        if (totalDiscount.compareTo(BigDecimal.ZERO) <= 0)
            return null;

        return AppliedPromotion.builder()
                .promotionId(promotion.getId())
                .promotionName(promotion.getName())
                .type(promotion.getType().name())
                .discountAmount(totalDiscount)
                .appliedToItems(itemDiscounts)
                .build();
    }

    private BigDecimal applyRule(PromotionRule rule, CalculateRequest request, List<ItemDiscount> itemDiscounts) {
        return switch (rule.getType()) {
            case PERCENTAGE_DISCOUNT -> applyPercentageDiscount(rule, request, itemDiscounts);
            case FIXED_DISCOUNT -> applyFixedDiscount(rule, request, itemDiscounts);
            case FREE_SHIPPING -> request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;
            case BUY_X_GET_Y -> BigDecimal.ZERO; // TODO: implement buy X get Y
        };
    }

    private BigDecimal applyPercentageDiscount(PromotionRule rule, CalculateRequest request,
            List<ItemDiscount> discounts) {
        if (rule.getDiscountPercent() == null)
            return BigDecimal.ZERO;

        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (CartItem item : request.getItems()) {
            BigDecimal discount = item.getSubtotal()
                    .multiply(rule.getDiscountPercent())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

            // Apply per-item max cap if configured
            if (rule.getMaxDiscountAmount() != null) {
                discount = discount.min(rule.getMaxDiscountAmount());
            }

            discounts.add(ItemDiscount.builder()
                    .sku(item.getSku())
                    .originalPrice(item.getSubtotal())
                    .discountAmount(discount)
                    .finalPrice(item.getSubtotal().subtract(discount))
                    .build());
            totalDiscount = totalDiscount.add(discount);
        }

        // Respect total max discount cap
        if (rule.getMaxDiscountAmount() != null) {
            totalDiscount = totalDiscount.min(rule.getMaxDiscountAmount());
        }
        return totalDiscount;
    }

    private BigDecimal applyFixedDiscount(PromotionRule rule, CalculateRequest request, List<ItemDiscount> discounts) {
        if (rule.getDiscountAmount() == null)
            return BigDecimal.ZERO;

        // Check min order value
        if (rule.getMinOrderValue() != null && request.getSubtotal().compareTo(rule.getMinOrderValue()) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal applied = rule.getDiscountAmount().min(request.getSubtotal());
        return applied;
    }
}
