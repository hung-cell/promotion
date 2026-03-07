package org.example.promotion.management.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.example.promotion.common.enums.RuleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleResponse {

    private Long id;
    private Long promotionId;
    private RuleType type;
    private BigDecimal discountPercent;
    private BigDecimal maxDiscountAmount;
    private BigDecimal discountAmount;
    private BigDecimal minOrderValue;
    private String buyProductId;
    private Integer buyQuantity;
    private String getProductId;
    private Integer getQuantity;
    private Integer maxApplications;
    private List<String> applicableRegions;
    private List<String> applicableShippingMethods;
    private LocalDateTime createdAt;
}
