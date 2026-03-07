package org.example.promotion.management.exception;

public class InvalidStatusException extends BaseException {

    public InvalidStatusException(String message) {
        super(ErrorCode.INVALID_STATUS, message);
    }
}
