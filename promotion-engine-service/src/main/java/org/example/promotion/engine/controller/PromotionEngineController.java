package org.example.promotion.engine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.promotion.engine.dto.request.CalculateRequest;
import org.example.promotion.engine.dto.request.CommitRequest;
import org.example.promotion.engine.dto.request.ReleaseRequest;
import org.example.promotion.engine.dto.request.ReserveRequest;
import org.example.promotion.engine.dto.request.ValidateRequest;
import org.example.promotion.common.dto.response.ApiResponse;
import org.example.promotion.engine.dto.response.CalculateResponse;
import org.example.promotion.engine.dto.response.ReservationResponse;
import org.example.promotion.engine.dto.response.ValidateResponse;
import org.example.promotion.engine.cache.PromotionCacheService;
import org.example.promotion.engine.service.PromotionCalculationService;
import org.example.promotion.engine.service.PromotionReservationService;
import org.example.promotion.engine.service.PromotionValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/engine")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Promotion Engine", description = "APIs for calculating, validating, and reserving promotions")
public class PromotionEngineController {

    private final PromotionCalculationService calculationService;
    private final PromotionValidationService validationService;
    private final PromotionReservationService reservationService;
    private final PromotionCacheService cacheService;

    // ─── Calculate ────────────────────────────────────────────────────────────

    @Operation(summary = "Tính toán promotion cho giỏ hàng")
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<CalculateResponse>> calculate(@Valid @RequestBody CalculateRequest request) {
        log.info("POST /api/v1/engine/calculate - customerId={}", request.getCustomerId());
        CalculateResponse result = calculationService.calculate(request);
        return ResponseEntity.ok(ApiResponse.<CalculateResponse>builder()
                .success(true)
                .data(result)
                .build());
    }

    @Operation(summary = "Preview promotion cho giỏ hàng (không reserve quota)")
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<CalculateResponse>> preview(@Valid @RequestBody CalculateRequest request) {
        log.info("POST /api/v1/engine/preview - customerId={}", request.getCustomerId());
        // Preview is the same calculation but doesn't reserve quota
        CalculateResponse result = calculationService.calculate(request);
        return ResponseEntity.ok(ApiResponse.<CalculateResponse>builder()
                .success(true)
                .data(result)
                .build());
    }

    // ─── Validate ─────────────────────────────────────────────────────────────

    @Operation(summary = "Validate promotion có thể áp dụng")
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<ValidateResponse>> validate(@Valid @RequestBody ValidateRequest request) {
        log.info("POST /api/v1/engine/validate - promotionIds={}", request.getPromotionIds());
        ValidateResponse result = validationService.validate(request);
        return ResponseEntity.ok(ApiResponse.<ValidateResponse>builder()
                .success(true)
                .data(result)
                .build());
    }

    // ─── Reserve / Commit / Release ───────────────────────────────────────────

    @Operation(summary = "Reserve promotion quota khi checkout")
    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<ReservationResponse>> reserve(@Valid @RequestBody ReserveRequest request) {
        log.info("POST /api/v1/engine/reserve - orderId={}", request.getOrderId());
        ReservationResponse result = reservationService.reserve(request);
        return ResponseEntity.ok(ApiResponse.<ReservationResponse>builder()
                .success(true)
                .data(result)
                .message("Promotion quota reserved successfully")
                .build());
    }

    @Operation(summary = "Commit promotion usage sau khi đặt hàng thành công")
    @PostMapping("/commit")
    public ResponseEntity<ApiResponse<Void>> commit(@Valid @RequestBody CommitRequest request) {
        log.info("POST /api/v1/engine/commit - reservationId={}", request.getReservationId());
        reservationService.commit(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Promotion usage committed successfully")
                .build());
    }

    @Operation(summary = "Release reservation khi hủy đơn hàng")
    @PostMapping("/release")
    public ResponseEntity<ApiResponse<Void>> release(@Valid @RequestBody ReleaseRequest request) {
        log.info("POST /api/v1/engine/release - reservationId={}", request.getReservationId());
        reservationService.release(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Reservation released successfully")
                .build());
    }

    // ─── Cache Management ─────────────────────────────────────────────────────

    @Operation(summary = "Warm up cache thủ công")
    @PostMapping("/cache/warmup")
    public ResponseEntity<ApiResponse<String>> warmup() {
        log.info("POST /api/v1/engine/cache/warmup");
        cacheService.warmUpCache();
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Cache warmed up successfully")
                .build());
    }

    @Operation(summary = "Invalidate cache của 1 promotion")
    @DeleteMapping("/cache/{promotionId}")
    public ResponseEntity<ApiResponse<Void>> invalidateCache(@PathVariable Long promotionId) {
        cacheService.invalidate(promotionId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Cache invalidated for promotionId=" + promotionId)
                .build());
    }

    // ─── Health ───────────────────────────────────────────────────────────────

    @Operation(summary = "Health check")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .data("UP")
                .message("promotion-engine-service is running")
                .build());
    }
}
