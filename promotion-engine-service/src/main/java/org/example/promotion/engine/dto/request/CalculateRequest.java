package org.example.promotion.engine.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CalculateRequest {

    private String cartId;

    @NotBlank(message = "customerId là bắt buộc")
    private String customerId;

    @NotBlank(message = "channel là bắt buộc")
    private String channel;

    @NotEmpty(message = "Danh sách items không được trống")
    @Valid
    private List<CartItem> items;

    @NotNull(message = "subtotal là bắt buộc")
    @DecimalMin(value = "0", message = "subtotal phải >= 0")
    private BigDecimal subtotal;

    private BigDecimal shippingFee;
    private String customerSegment;
    private String customerTier;
    private List<String> appliedCouponCodes;
    private String shippingRegion;
    private String shippingMethod;

    @Data
    public static class CartItem {
        @NotBlank
        private String sku;
        @NotBlank
        private String productId;
        private String productName;
        private String categoryId;
        private String brandId;
        @NotNull
        private Integer quantity;
        @NotNull
        private BigDecimal unitPrice;
        @NotNull
        private BigDecimal subtotal;
    }
}
