package org.example.promotion.service;

import org.example.promotion.dto.request.RedeemCouponRequest;
import org.example.promotion.dto.request.ReserveCouponRequest;
import org.example.promotion.dto.request.ValidateCouponRequest;
import org.example.promotion.dto.request.CreateCouponRequest;
import org.example.promotion.dto.response.CouponResponse;
import org.example.promotion.dto.response.CouponReservationResponse;
import org.example.promotion.dto.response.CouponValidationResponse;

public interface ICouponService {
    CouponResponse createCoupon(CreateCouponRequest request);

    CouponValidationResponse validateCoupon(ValidateCouponRequest request);

    CouponReservationResponse reserveCoupon(ReserveCouponRequest request);

    void redeemCoupon(RedeemCouponRequest request);

    void releaseCoupon(Long reservationId);
}
