package org.example.promotion.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponValidationResponse {

    private boolean valid;
    private String code;
    private String promotionName;
    private BigDecimal discountValue;
    private String discountType;
    private BigDecimal maxDiscountAmount;
    private String message;
}
