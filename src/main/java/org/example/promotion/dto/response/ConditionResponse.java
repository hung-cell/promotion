package org.example.promotion.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.example.promotion.enums.ConditionType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConditionResponse {

    private Long id;
    private Long promotionId;
    private ConditionType conditionType;
    private Object conditionValue; // Parsed JSON
    private String operator;
    private LocalDateTime createdAt;
}
