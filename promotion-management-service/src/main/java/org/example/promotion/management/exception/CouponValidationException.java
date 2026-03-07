package org.example.promotion.management.exception;

public class CouponValidationException extends BaseException {

    public CouponValidationException(String message) {
        super(ErrorCode.COUPON_VALIDATION_ERROR, message);
    }
}
