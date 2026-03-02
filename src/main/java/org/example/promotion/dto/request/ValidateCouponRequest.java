package org.example.promotion.dto.request;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateCouponRequest {

    @NotBlank(message = "Vui lòng nhập mã giảm giá")
    private String code;

    @NotBlank(message = "Vui lòng nhập mã khách hàng")
    private String customerId;

    private BigDecimal orderAmount;
}
