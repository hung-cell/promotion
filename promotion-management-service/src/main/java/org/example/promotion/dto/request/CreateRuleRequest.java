package org.example.promotion.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import org.example.promotion.enums.RuleType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating a PromotionRule.
 * Cross-field validation ensures required fields are present per RuleType.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRuleRequest {

    @NotNull(message = "Vui lòng chọn loại quy tắc")
    private RuleType type;

    // PERCENTAGE_DISCOUNT
    @DecimalMin(value = "0.01", message = "Phần trăm giảm phải lớn hơn 0")
    @DecimalMax(value = "100.00", message = "Phần trăm giảm không được vượt quá 100%")
    private BigDecimal discountPercent;

    @DecimalMin(value = "0", inclusive = true, message = "Mức giảm tối đa phải >= 0")
    private BigDecimal maxDiscountAmount;

    // FIXED_DISCOUNT
    @DecimalMin(value = "0.01", message = "Số tiền giảm phải lớn hơn 0")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0", inclusive = true, message = "Giá trị đơn hàng tối thiểu phải >= 0")
    private BigDecimal minOrderValue;

    // BUY_X_GET_Y
    private String buyProductId;

    @Min(value = 1, message = "Số lượng mua phải >= 1")
    private Integer buyQuantity;

    private String getProductId;

    @Min(value = 1, message = "Số lượng tặng phải >= 1")
    private Integer getQuantity;

    @Min(value = 1, message = "Số lần áp dụng tối đa phải >= 1")
    private Integer maxApplications;

    // FREE_SHIPPING
    private List<String> applicableRegions;
    private List<String> applicableShippingMethods;

    // =================== Cross-field validations by RuleType ===================

    @AssertTrue(message = "Loại PERCENTAGE_DISCOUNT yêu cầu phải có discountPercent")
    public boolean isPercentageDiscountValid() {
        if (type != RuleType.PERCENTAGE_DISCOUNT)
            return true;
        return discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0;
    }

    @AssertTrue(message = "Loại FIXED_DISCOUNT yêu cầu phải có discountAmount")
    public boolean isFixedDiscountValid() {
        if (type != RuleType.FIXED_DISCOUNT)
            return true;
        return discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    @AssertTrue(message = "Loại BUY_X_GET_Y yêu cầu buyProductId, buyQuantity, getProductId, getQuantity")
    public boolean isBuyXGetYValid() {
        if (type != RuleType.BUY_X_GET_Y)
            return true;
        return buyProductId != null && !buyProductId.isBlank()
                && buyQuantity != null && buyQuantity >= 1
                && getProductId != null && !getProductId.isBlank()
                && getQuantity != null && getQuantity >= 1;
    }
}
