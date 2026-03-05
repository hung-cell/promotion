package org.example.promotion.engine.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReservationResponse {

    private String reservationId;
    private String orderId;
    private List<ReservedPromotion> reservedPromotions;
    private LocalDateTime expiresAt;
    private Integer ttlSeconds;

    @Data
    @Builder
    public static class ReservedPromotion {
        private Long promotionId;
        private boolean reserved;
        private LocalDateTime reservedAt;
        private String reason; // If not reserved, why
    }
}
