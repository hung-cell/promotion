package org.example.promotion.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {
    private Long id;
    private Long promotionId;
    private String code;
    private Integer maxUses;
    private Integer currentUses;
    private LocalDateTime expiryDate;
    private String status;
}
