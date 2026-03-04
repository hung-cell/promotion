package org.example.promotion.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // General
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Đã xảy ra lỗi không xác định"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Dữ liệu không hợp lệ"),

    // Resource
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy dữ liệu"),

    // HTTP Method
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Phương thức HTTP không được hỗ trợ"),

    // Promotion
    INVALID_STATUS(HttpStatus.BAD_REQUEST, "INVALID_STATUS", "Trạng thái khuyến mãi không hợp lệ"),

    // Coupon
    COUPON_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COUPON_VALIDATION_ERROR", "Lỗi kiểm tra mã giảm giá"),

    // Concurrency / Conflict
    CONCURRENT_UPDATE_ERROR(HttpStatus.CONFLICT, "CONCURRENT_UPDATE_ERROR",
            "Dữ liệu đã được cập nhật bởi một giao dịch khác"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", "Dữ liệu đã tồn tại trong hệ thống");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String code, String defaultMessage) {
        this.status = status;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
