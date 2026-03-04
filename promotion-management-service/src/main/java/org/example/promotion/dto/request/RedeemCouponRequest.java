package org.example.promotion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * Request DTO for redeeming a coupon reservation.
 * reservationId is a Long to ensure type safety and prevent
 * NumberFormatException.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedeemCouponRequest {

    @NotNull(message = "Vui lòng nhập mã lượt giữ")
    private Long reservationId;

    @NotBlank(message = "Vui lòng nhập mã đơn hàng")
    private String orderId;
}
