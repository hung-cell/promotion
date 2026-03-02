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
public class StackingConfigResponse {

    private Long promotionId;
    private Boolean stackable;
    private Integer priority;
    private String exclusiveGroupId;
    private BigDecimal maxStackDiscount;
}
