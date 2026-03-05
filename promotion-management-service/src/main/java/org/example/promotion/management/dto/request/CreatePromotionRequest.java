package org.example.promotion.management.dto.request;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.example.promotion.common.enums.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePromotionRequest {

    @NotBlank(message = "Vui lòng nhập tên chương trình khuyến mãi")
    @Size(max = 255, message = "Tên chương trình khuyến mãi tối đa 255 ký tự")
    private String name;

    private String description;

    @NotNull(message = "Vui lòng chọn loại khuyến mãi")
    private PromotionType type;

    @NotNull(message = "Vui lòng nhập giá trị giảm")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    @DecimalMax(value = "100.00", message = "Giá trị giảm phần trăm không được vượt quá 100")
    private BigDecimal discountValue;

    @DecimalMin(value = "0", message = "Mức giảm tối đa phải >= 0")
    private BigDecimal maxDiscountAmount;

    @DecimalMin(value = "0", message = "Giá trị đơn hàng tối thiểu phải >= 0")
    private BigDecimal minOrderValue;

    @NotNull(message = "Vui lòng chọn ngày bắt đầu")
    private LocalDateTime startDate;

    @NotNull(message = "Vui lòng chọn ngày kết thúc")
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
