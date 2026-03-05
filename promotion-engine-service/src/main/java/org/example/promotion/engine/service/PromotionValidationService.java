package org.example.promotion.engine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.promotion.engine.cache.PromotionCacheService;
import org.example.promotion.engine.dto.request.ValidateRequest;
import org.example.promotion.engine.dto.response.ValidateResponse;
import org.example.promotion.engine.dto.response.ValidateResponse.PromotionValidationResult;
import org.example.promotion.engine.dto.response.ValidateResponse.CouponValidationResult;
import org.example.promotion.engine.entity.Promotion;
import org.example.promotion.engine.repository.CouponRepository;
import org.example.promotion.common.enums.PromotionStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionValidationService {

    private final PromotionCacheService cacheService;
    private final CouponRepository couponRepository;

    public ValidateResponse validate(ValidateRequest request) {
        log.info("[Validate] orderId={}, promotionIds={}, couponCodes={}", request.getOrderId(),
                request.getPromotionIds(), request.getCouponCodes());

        List<PromotionValidationResult> promotionResults = validatePromotions(request);
        List<CouponValidationResult> couponResults = validateCoupons(request);
        List<String> conflicts = detectConflicts(request.getPromotionIds());
        List<String> warnings = buildWarnings(request);

        boolean allValid = promotionResults.stream().allMatch(PromotionValidationResult::isValid)
                && couponResults.stream().allMatch(CouponValidationResult::isValid)
                && conflicts.isEmpty();

        return ValidateResponse.builder()
                .valid(allValid)
                .validPromotions(promotionResults)
                .validCoupons(couponResults)
                .conflicts(conflicts)
                .warnings(warnings)
                .build();
    }

    private List<PromotionValidationResult> validatePromotions(ValidateRequest request) {
        List<PromotionValidationResult> results = new ArrayList<>();
        if (request.getPromotionIds() == null)
            return results;

        for (Long promotionId : request.getPromotionIds()) {
            var optPromotion = cacheService.getPromotion(promotionId);
            if (optPromotion.isEmpty()) {
                results.add(PromotionValidationResult.builder()
                        .promotionId(promotionId)
                        .valid(false)
                        .reason("PROMOTION_NOT_FOUND")
                        .message("Không tìm thấy chương trình khuyến mãi")
                        .build());
                continue;
            }

            Promotion p = optPromotion.get();
            String failReason = getInvalidReason(p, request);
            results.add(PromotionValidationResult.builder()
                    .promotionId(promotionId)
                    .valid(failReason == null)
                    .reason(failReason)
                    .message(failReason != null ? reasonToVietnamese(failReason) : null)
                    .build());
        }
        return results;
    }

    private String getInvalidReason(Promotion p, ValidateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        if (!PromotionStatus.ACTIVE.equals(p.getStatus()))
            return "PROMOTION_INACTIVE";
        if (p.getEndDate().isBefore(now))
            return "PROMOTION_EXPIRED";
        if (p.getStartDate().isAfter(now))
            return "PROMOTION_NOT_STARTED";
        if (p.getUsageLimit() != null) {
            Long remaining = cacheService.getRemainingQuota(p.getId());
            if (remaining != null && remaining <= 0)
                return "USAGE_LIMIT_EXCEEDED";
        }
        if (p.getMinOrderValue() != null && request.getSubtotal() != null
                && request.getSubtotal().compareTo(p.getMinOrderValue()) < 0) {
            return "MIN_ORDER_VALUE_NOT_MET";
        }
        return null;
    }

    private List<CouponValidationResult> validateCoupons(ValidateRequest request) {
        List<CouponValidationResult> results = new ArrayList<>();
        if (request.getCouponCodes() == null)
            return results;

        for (String code : request.getCouponCodes()) {
            couponRepository.findByCode(code).ifPresentOrElse(
                    coupon -> {
                        boolean valid = "ACTIVE".equals(coupon.getStatus().name())
                                && coupon.getCurrentUses() < coupon.getMaxUses()
                                && (coupon.getExpiryDate() == null
                                        || coupon.getExpiryDate().isAfter(LocalDateTime.now()));
                        results.add(CouponValidationResult.builder()
                                .couponCode(code)
                                .valid(valid)
                                .promotionId(valid ? coupon.getPromotion().getId() : null)
                                .reason(valid ? null : "COUPON_EXPIRED_OR_USED")
                                .message(valid ? null : "Mã giảm giá đã hết hạn hoặc đã được sử dụng")
                                .build());
                    },
                    () -> results.add(CouponValidationResult.builder()
                            .couponCode(code).valid(false)
                            .reason("COUPON_NOT_FOUND").message("Mã giảm giá không tồn tại")
                            .build()));
        }
        return results;
    }

    private List<String> detectConflicts(List<Long> promotionIds) {
        // TODO: Implement stacking rule conflict detection
        return List.of();
    }

    private List<String> buildWarnings(ValidateRequest request) {
        List<String> warnings = new ArrayList<>();
        if (request.getSubtotal() != null && request.getSubtotal().compareTo(BigDecimal.valueOf(500000)) < 0) {
            warnings.add("Đơn hàng có thể chưa đạt giá trị tối thiểu cho một số ưu đãi");
        }
        return warnings;
    }

    private String reasonToVietnamese(String reason) {
        return switch (reason) {
            case "PROMOTION_EXPIRED" -> "Chương trình khuyến mãi đã hết hạn";
            case "PROMOTION_NOT_STARTED" -> "Chương trình khuyến mãi chưa bắt đầu";
            case "PROMOTION_INACTIVE" -> "Chương trình khuyến mãi không hoạt động";
            case "USAGE_LIMIT_EXCEEDED" -> "Đã hết lượt sử dụng";
            case "MIN_ORDER_VALUE_NOT_MET" -> "Đơn hàng chưa đạt giá trị tối thiểu";
            default -> "Không hợp lệ";
        };
    }
}
