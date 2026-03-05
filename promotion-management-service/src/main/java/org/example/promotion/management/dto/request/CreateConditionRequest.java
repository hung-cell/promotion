package org.example.promotion.management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.promotion.common.enums.ConditionType;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateConditionRequest {

    @NotNull(message = "Vui lòng chọn loại điều kiện")
    private ConditionType type;

    // PRODUCT conditions
    private List<String> includeSkus;
    private List<String> includeCategoryIds;
    private List<String> includeBrandIds;
    private List<String> excludeSkus;

    // CUSTOMER conditions
    private List<String> customerSegments;
    private List<String> customerTiers;
    private Boolean firstPurchaseOnly;

    // ORDER conditions
    private BigDecimal minOrderValue;
    private Integer minQuantity;
    private Integer maxUsagePerCustomer;
    private Integer maxTotalUsage;

    // CHANNEL conditions
    private List<String> includeChannels;
    private List<String> excludeChannels;
}
