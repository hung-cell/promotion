package org.example.promotion.management.service;

import org.example.promotion.management.dto.request.RedeemCouponRequest;
import org.example.promotion.management.dto.request.ReserveCouponRequest;
import org.example.promotion.management.dto.request.ValidateCouponRequest;
import org.example.promotion.management.dto.request.CreateCouponRequest;
import org.example.promotion.management.dto.response.CouponResponse;
import org.example.promotion.management.dto.response.CouponReservationResponse;
import org.example.promotion.management.dto.response.CouponValidationResponse;

public interface ICouponService {
    CouponResponse createCoupon(CreateCouponRequest request);

    CouponValidationResponse validateCoupon(ValidateCouponRequest request);

    CouponReservationResponse reserveCoupon(ReserveCouponRequest request);

    void redeemCoupon(RedeemCouponRequest request);

    void releaseCoupon(Long reservationId);
}
