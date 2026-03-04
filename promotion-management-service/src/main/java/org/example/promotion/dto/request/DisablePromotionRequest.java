package org.example.promotion.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisablePromotionRequest {

    private String reason;
}
