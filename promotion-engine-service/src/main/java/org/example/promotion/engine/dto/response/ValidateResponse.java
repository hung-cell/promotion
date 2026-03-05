package org.example.promotion.engine.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ValidateResponse {

    private boolean valid;
    private List<PromotionValidationResult> validPromotions;
    private List<CouponValidationResult> validCoupons;
    private List<String> conflicts;
    private List<String> warnings;

    @Data
    @Builder
    public static class PromotionValidationResult {
        private Long promotionId;
        private boolean valid;
        private String reason;
        private String message;
    }

    @Data
    @Builder
    public static class CouponValidationResult {
        private String couponCode;
        private boolean valid;
        private Long promotionId;
        private String reason;
        private String message;
    }
}
