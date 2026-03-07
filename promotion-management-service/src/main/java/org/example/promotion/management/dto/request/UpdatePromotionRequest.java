package org.example.promotion.management.dto.request;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for updating an existing promotion.
 * All fields are optional — only non-null fields will be applied (partial
 * update).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePromotionRequest {

    @Size(min = 1, max = 255, message = "Tên chương trình khuyến mãi không được để trống và tối đa 255 ký tự")
    private String name;

    private String description;

    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0", inclusive = true, message = "Mức giảm tối đa phải >= 0")
    private BigDecimal maxDiscountAmount;

    @DecimalMin(value = "0", inclusive = true, message = "Giá trị đơn hàng tối thiểu phải >= 0")
    private BigDecimal minOrderValue;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Min(value = 1, message = "Số lượt sử dụng tối đa phải >= 1")
    private Integer usageLimit;

    @Min(value = 1, message = "Số lượt sử dụng mỗi khách hàng phải >= 1")
    private Integer usageLimitPerCustomer;

    @AssertTrue(message = "Ngày kết thúc phải sau ngày bắt đầu")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null)
            return true;
        return endDate.isAfter(startDate);
    }
}
