package org.example.promotion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.promotion.dto.request.RedeemCouponRequest;
import org.example.promotion.dto.request.ReserveCouponRequest;
import org.example.promotion.dto.request.ValidateCouponRequest;
import org.example.promotion.dto.request.CreateCouponRequest;
import org.example.promotion.dto.response.ApiResponse;
import org.example.promotion.dto.response.CouponReservationResponse;
import org.example.promotion.dto.response.CouponValidationResponse;
import org.example.promotion.dto.response.CouponResponse;
import org.example.promotion.service.ICouponService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final ICouponService couponService;

    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CreateCouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Tạo mã giảm giá thành công"));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<CouponValidationResponse>> validateCoupon(
            @Valid @RequestBody ValidateCouponRequest request) {
        CouponValidationResponse response = couponService.validateCoupon(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Mã giảm giá hợp lệ"));
    }

    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<CouponReservationResponse>> reserveCoupon(
            @Valid @RequestBody ReserveCouponRequest request) {
        CouponReservationResponse response = couponService.reserveCoupon(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Giữ mã giảm giá thành công"));
    }

    @PostMapping("/redeem")
    public ResponseEntity<ApiResponse<Void>> redeemCoupon(
            @Valid @RequestBody RedeemCouponRequest request) {
        couponService.redeemCoupon(request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Sử dụng mã giảm giá thành công"));
    }

    @PostMapping("/release/{reservationId}")
    public ResponseEntity<ApiResponse<Void>> releaseCoupon(
            @PathVariable Long reservationId) {
        couponService.releaseCoupon(reservationId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Giải phóng mã giảm giá thành công"));
    }
}
