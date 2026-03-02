package org.example.promotion.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponReservationResponse {

    private Long reservationId;
    private String code;
    private String customerId;
    private String status;
    private LocalDateTime reservedAt;
    private LocalDateTime expiresAt;
}
