package org.example.promotion.management.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisablePromotionRequest {

    private String reason;
}
