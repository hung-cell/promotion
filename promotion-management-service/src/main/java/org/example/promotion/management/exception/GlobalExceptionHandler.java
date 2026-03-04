package org.example.promotion.exception;

import org.example.promotion.dto.response.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(BaseException.class)
        public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
                Map<String, String> error = new HashMap<>();
                error.put("code", ex.getErrorCode().getCode());
                error.put("message", ex.getMessage());
                return ResponseEntity.status(ex.getErrorCode().getStatus())
                                .body(ApiResponse.error(ex.getMessage(), error));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
                List<Map<String, String>> details = ex.getBindingResult().getFieldErrors().stream()
                                .map(fieldError -> {
                                        Map<String, String> detail = new HashMap<>();
                                        detail.put("field", fieldError.getField());
                                        detail.put("message", fieldError.getDefaultMessage());
                                        return detail;
                                })
                                .collect(Collectors.toList());

                // Also capture global errors (@AssertTrue cross-field violations)
                ex.getBindingResult().getGlobalErrors().forEach(globalError -> {
                        Map<String, String> detail = new HashMap<>();
                        detail.put("field", globalError.getObjectName());
                        detail.put("message", globalError.getDefaultMessage());
                        details.add(detail);
                });

                Map<String, Object> error = new HashMap<>();
                error.put("code", ErrorCode.VALIDATION_ERROR.getCode());
                error.put("message", ErrorCode.VALIDATION_ERROR.getDefaultMessage());
                error.put("details", details);

                return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus())
                                .body(ApiResponse.error("Dữ liệu đầu vào không hợp lệ", error));
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
                log.warn("Type mismatch for parameter '{}': value='{}', expected='{}'",
                                ex.getName(), ex.getValue(),
                                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

                String message = String.format("Giá trị '%s' không hợp lệ cho tham số '%s'", ex.getValue(),
                                ex.getName());
                Map<String, String> error = new HashMap<>();
                error.put("code", ErrorCode.VALIDATION_ERROR.getCode());
                error.put("message", message);
                return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus())
                                .body(ApiResponse.error("Giá trị tham số không hợp lệ", error));
        }

        @ExceptionHandler(OptimisticLockingFailureException.class)
        public ResponseEntity<ApiResponse<Void>> handleOptimisticLocking(OptimisticLockingFailureException ex) {
                log.warn("Optimistic locking failure: {}", ex.getMessage());
                Map<String, String> error = new HashMap<>();
                error.put("code", ErrorCode.CONCURRENT_UPDATE_ERROR.getCode());
                error.put("message", ErrorCode.CONCURRENT_UPDATE_ERROR.getDefaultMessage());
                return ResponseEntity.status(ErrorCode.CONCURRENT_UPDATE_ERROR.getStatus())
                                .body(ApiResponse.error(ErrorCode.CONCURRENT_UPDATE_ERROR.getDefaultMessage(), error));
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
                log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
                Map<String, String> error = new HashMap<>();
                error.put("code", ErrorCode.DUPLICATE_RESOURCE.getCode());
                error.put("message", "Dữ liệu vi phạm ràng buộc duy nhất hoặc khóa ngoại");
                return ResponseEntity.status(ErrorCode.DUPLICATE_RESOURCE.getStatus())
                                .body(ApiResponse.error(ErrorCode.DUPLICATE_RESOURCE.getDefaultMessage(), error));
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
                log.warn("Message parsing failed: {}", ex.getMessage());
                Map<String, String> error = new HashMap<>();
                error.put("code", ErrorCode.VALIDATION_ERROR.getCode());
                error.put("message", "Định dạng JSON không hợp lệ hoặc thiếu trường bắt buộc");
                return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus())
                                .body(ApiResponse.error("Lỗi dữ liệu payload", error));
        }

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException ex) {
                log.warn("Resource not found: {}", ex.getMessage());
                Map<String, String> error = new HashMap<>();
                error.put("code", ErrorCode.RESOURCE_NOT_FOUND.getCode());
                error.put("message", "Không tìm thấy API Endpoint: " + ex.getResourcePath());
                return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.getStatus())
                                .body(ApiResponse.error("URL yêu cầu không tồn tại trên máy chủ.", error));
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
                log.warn("Method not supported: {}", ex.getMessage());
                String supportedMethods = ex.getSupportedHttpMethods() != null
                                ? ex.getSupportedHttpMethods().toString()
                                : "Không rõ";
                Map<String, String> error = new HashMap<>();
                error.put("code", ErrorCode.METHOD_NOT_ALLOWED.getCode());
                error.put("message", "Các phương thức được hỗ trợ: " + supportedMethods);
                return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getStatus())
                                .body(ApiResponse.error(
                                                "Phương thức HTTP '" + ex.getMethod()
                                                                + "' không được hỗ trợ cho endpoint này.",
                                                error));
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ApiResponse<Void>> handleMissingParams(MissingServletRequestParameterException ex) {
                log.warn("Missing parameter: {}", ex.getMessage());
                Map<String, String> error = new HashMap<>();
                error.put("code", ErrorCode.VALIDATION_ERROR.getCode());
                error.put("message", "Thiếu tham số bắt buộc '" + ex.getParameterName() + "'");
                return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus())
                                .body(ApiResponse.error("Thiếu tham số Request", error));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
                String correlationId = UUID.randomUUID().toString();
                log.error("Unhandled exception occurred [correlationId={}]", correlationId, ex);

                Map<String, String> error = new HashMap<>();
                error.put("code", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
                error.put("message", "Đã xảy ra lỗi hệ thống. Mã tham chiếu: " + correlationId);

                return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                                .body(ApiResponse.error("Lỗi máy chủ", error));
        }
}
