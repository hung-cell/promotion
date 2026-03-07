package org.example.promotion.engine.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ValidateRequest {

    private String orderId;

    @NotBlank(message = "customerId là bắt buộc")
    private String customerId;

    private String channel;

    @NotEmpty(message = "Danh sách items không được trống")
    private List<CalculateRequest.CartItem> items;

    private BigDecimal subtotal;

    private List<Long> promotionIds;

    private List<String> couponCodes;
}
