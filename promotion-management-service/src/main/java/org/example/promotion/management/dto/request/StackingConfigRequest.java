package org.example.promotion.management.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StackingConfigRequest {

    private Boolean stackable;
    private Integer priority;
    private String exclusiveGroupId;
    private BigDecimal maxStackDiscount;
}
