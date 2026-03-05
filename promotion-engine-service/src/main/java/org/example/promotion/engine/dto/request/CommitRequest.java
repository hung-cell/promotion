package org.example.promotion.engine.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommitRequest {

    @NotBlank(message = "reservationId là bắt buộc")
    private String reservationId;

    @NotBlank(message = "orderId là bắt buộc")
    private String orderId;

    @NotBlank(message = "customerId là bắt buộc")
    private String customerId;

    private BigDecimal finalAmount;

    private BigDecimal discountAmount;
}
