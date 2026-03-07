package org.example.promotion.engine.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CalculateResponse {

    private String calculationId;
    private LocalDateTime timestamp;
    private List<AppliedPromotion> applicablePromotions;
    private CalculationSummary summary;
    private List<String> messages;

    @Data
    @Builder
    public static class AppliedPromotion {
        private Long promotionId;
        private String promotionName;
        private String type;
        private BigDecimal discountAmount;
        private List<ItemDiscount> appliedToItems;
        private Integer priority;
        private String couponCode;
    }

    @Data
    @Builder
    public static class ItemDiscount {
        private String sku;
        private BigDecimal originalPrice;
        private BigDecimal discountAmount;
        private BigDecimal finalPrice;
    }

    @Data
    @Builder
    public static class CalculationSummary {
        private BigDecimal originalSubtotal;
        private BigDecimal totalDiscount;
        private BigDecimal finalSubtotal;
        private BigDecimal originalShippingFee;
        private BigDecimal shippingDiscount;
        private BigDecimal finalShippingFee;
        private BigDecimal totalAmount;
        private BigDecimal savedAmount;
        private Double savedPercentage;
    }
}
