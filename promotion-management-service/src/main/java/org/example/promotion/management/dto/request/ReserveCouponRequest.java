package org.example.promotion.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReserveCouponRequest {

    @NotBlank(message = "Vui lòng nhập mã giảm giá")
    private String code;

    @NotBlank(message = "Vui lòng nhập mã khách hàng")
    private String customerId;

    private String orderId;
}
