package org.example.promotion.engine.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ReserveRequest {

    @NotBlank(message = "orderId là bắt buộc")
    private String orderId;

    @NotBlank(message = "customerId là bắt buộc")
    private String customerId;

    @NotEmpty(message = "promotionIds không được trống")
    private List<Long> promotionIds;

    private List<String> couponCodes;

    private String calculationId;

    private Integer ttlSeconds = 600; // Default 10 phút
}
