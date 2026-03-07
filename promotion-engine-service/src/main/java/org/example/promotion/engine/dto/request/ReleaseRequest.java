package org.example.promotion.engine.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReleaseRequest {

    @NotBlank(message = "reservationId là bắt buộc")
    private String reservationId;

    @NotBlank(message = "orderId là bắt buộc")
    private String orderId;

    private String reason;
}
