package org.example.promotion.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCouponRequest {

    @NotNull(message = "Vui lòng nhập mã chương trình khuyến mãi")
    private Long promotionId;

    @NotBlank(message = "Vui lòng nhập mã giảm giá")
    @Size(min = 3, max = 100, message = "Mã giảm giá phải từ 3 đến 100 ký tự")
    @Pattern(regexp = "^[A-Z0-9_\\-]+$", message = "Mã giảm giá chỉ được chứa chữ hoa, số, gạch dưới (_) và gạch ngang (-)")
    private String code;

    @Min(value = 1, message = "Số lượt sử dụng tối đa phải >= 1")
    private Integer maxUses;

    private LocalDateTime expiryDate;
}
