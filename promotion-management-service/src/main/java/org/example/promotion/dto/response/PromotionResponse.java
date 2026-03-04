package org.example.promotion.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.example.promotion.enums.PromotionStatus;
import org.example.promotion.enums.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PromotionResponse {

    private Long id;
    private String name;
    private String description;
    private PromotionType type;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usageLimitPerCustomer;
    private Integer usageCount;
    private PromotionStatus status;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Chi tiết (chỉ có khi gọi getById)
    private List<RuleResponse> rules;
    private List<ConditionResponse> conditions;
    private StackingConfigResponse stackingConfig;
}
