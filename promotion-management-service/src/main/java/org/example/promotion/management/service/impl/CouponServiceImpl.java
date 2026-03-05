package org.example.promotion.management.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.promotion.common.AppConstants;
import org.example.promotion.management.dto.request.RedeemCouponRequest;
import org.example.promotion.management.dto.request.ReserveCouponRequest;
import org.example.promotion.management.dto.request.ValidateCouponRequest;
import org.example.promotion.management.dto.request.CreateCouponRequest;
import org.example.promotion.management.dto.response.CouponReservationResponse;
import org.example.promotion.management.dto.response.CouponValidationResponse;
import org.example.promotion.management.dto.response.CouponResponse;
import org.example.promotion.management.entity.Coupon;
import org.example.promotion.management.entity.CouponReservation;
import org.example.promotion.management.entity.CouponUsage;
import org.example.promotion.management.entity.Promotion;
import org.example.promotion.common.enums.CouponStatus;
import org.example.promotion.common.enums.PromotionStatus;
import org.example.promotion.common.enums.ReservationStatus;
import org.example.promotion.management.exception.CouponValidationException;
import org.example.promotion.management.exception.ResourceNotFoundException;
import org.example.promotion.management.repository.CouponRepository;
import org.example.promotion.management.repository.CouponReservationRepository;
import org.example.promotion.management.repository.CouponUsageRepository;
import org.example.promotion.management.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import org.example.promotion.management.service.ICouponService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl implements ICouponService {

    private final CouponRepository couponRepository;
    private final CouponReservationRepository reservationRepository;
    private final CouponUsageRepository usageRepository;
    private final PromotionRepository promotionRepository;

    // =================== Create ===================

    @Transactional
    @Override
    public CouponResponse createCoupon(CreateCouponRequest request) {
        Promotion promotion = promotionRepository.findById(request.getPromotionId())
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", request.getPromotionId()));

        if (couponRepository.findByCode(request.getCode()).isPresent()) {
            throw new CouponValidationException("Mã giảm giá đã tồn tại");
        }

        Coupon coupon = Coupon.builder()
                .promotion(promotion)
                .code(request.getCode())
                .maxUses(request.getMaxUses() != null ? request.getMaxUses() : AppConstants.DEFAULT_COUPON_MAX_USES)
                .expiryDate(request.getExpiryDate() != null ? request.getExpiryDate() : promotion.getEndDate())
                .status(CouponStatus.ACTIVE)
                .build();

        coupon = couponRepository.save(coupon);
        log.info("Coupon created: id={}, code={}", coupon.getId(), coupon.getCode());

        return CouponResponse.builder()
                .id(coupon.getId())
                .promotionId(promotion.getId())
                .code(coupon.getCode())
                .maxUses(coupon.getMaxUses())
                .currentUses(coupon.getCurrentUses())
                .expiryDate(coupon.getExpiryDate())
                .status(coupon.getStatus().name())
                .build();
    }

    // =================== Validate ===================

    @Override
    public CouponValidationResponse validateCoupon(ValidateCouponRequest request) {
        Coupon coupon = validateAndGetCoupon(request);
        Promotion promotion = coupon.getPromotion();

        log.info("Coupon validated successfully: code={}, customerId={}", request.getCode(), request.getCustomerId());

        return CouponValidationResponse.builder()
                .valid(true)
                .code(coupon.getCode())
                .promotionName(promotion.getName())
                .discountValue(promotion.getDiscountValue())
                .discountType(promotion.getType().name())
                .maxDiscountAmount(promotion.getMaxDiscountAmount())
                .message("Coupon is valid")
                .build();
    }

    /**
     * Validates coupon and returns the Coupon entity for reuse by other methods.
     */
    private Coupon validateAndGetCoupon(ValidateCouponRequest request) {
        Coupon coupon = couponRepository.findByCode(request.getCode())
                .orElseThrow(() -> new CouponValidationException("Không tìm thấy mã giảm giá: " + request.getCode()));

        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new CouponValidationException(
                    "Mã giảm giá không ở trạng thái hoạt động. Trạng thái hiện tại: " + coupon.getStatus());
        }

        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new CouponValidationException("Mã giảm giá đã hết hạn sử dụng");
        }

        if (coupon.getMaxUses() != null && coupon.getCurrentUses() >= coupon.getMaxUses()) {
            throw new CouponValidationException("Mã giảm giá đã hết lượt sử dụng");
        }

        long customerUsage = usageRepository.countByCouponIdAndCustomerId(coupon.getId(), request.getCustomerId());
        Promotion promotion = coupon.getPromotion();
        if (promotion.getUsageLimitPerCustomer() != null && customerUsage >= promotion.getUsageLimitPerCustomer()) {
            throw new CouponValidationException("Bạn đã sử dụng mã giảm giá này quá số lần cho phép");
        }

        if (promotion.getStatus() != PromotionStatus.ACTIVE) {
            throw new CouponValidationException("Chương trình khuyến mãi liên kết không hoạt động");
        }

        if (promotion.getMinOrderValue() != null && request.getOrderAmount() != null
                && request.getOrderAmount().compareTo(promotion.getMinOrderValue()) < 0) {
            throw new CouponValidationException(
                    "Giá trị đơn hàng không đạt mức tối thiểu: " + promotion.getMinOrderValue());
        }

        return coupon;
    }

    // =================== Reserve ===================

    @Override
    @Transactional
    public CouponReservationResponse reserveCoupon(ReserveCouponRequest request) {
        // Validate và lấy coupon (tránh query lần 2)
        ValidateCouponRequest validateRequest = ValidateCouponRequest.builder()
                .code(request.getCode())
                .customerId(request.getCustomerId())
                .build();
        Coupon coupon = validateAndGetCoupon(validateRequest);

        // Check không có reservation đang active
        long activeReservations = reservationRepository.countByCouponIdAndCustomerIdAndStatus(
                coupon.getId(), request.getCustomerId(), ReservationStatus.RESERVED);
        if (activeReservations > 0) {
            throw new CouponValidationException("Bạn đã có một lượt giữ mã này đang hoạt động");
        }

        // Tạo reservation với TTL từ config
        LocalDateTime now = LocalDateTime.now();
        CouponReservation reservation = CouponReservation.builder()
                .coupon(coupon)
                .customerId(request.getCustomerId())
                .orderId(request.getOrderId())
                .status(ReservationStatus.RESERVED)
                .expiresAt(now.plusMinutes(AppConstants.RESERVATION_TTL_MINUTES))
                .build();
        reservation = reservationRepository.save(reservation);

        // Tăng usage count atomic (tránh race condition)
        couponRepository.incrementCurrentUses(coupon.getId(), 1);

        log.info("Coupon reserved: code={}, reservationId={}, customerId={}",
                request.getCode(), reservation.getId(), request.getCustomerId());

        return CouponReservationResponse.builder()
                .reservationId(reservation.getId())
                .code(coupon.getCode())
                .customerId(request.getCustomerId())
                .status(ReservationStatus.RESERVED.name())
                .reservedAt(reservation.getCreatedAt())
                .expiresAt(reservation.getExpiresAt())
                .build();
    }

    // =================== Redeem ===================

    @Override
    @Transactional
    public void redeemCoupon(RedeemCouponRequest request) {
        CouponReservation reservation = reservationRepository
                .findByIdAndStatus(request.getReservationId(), ReservationStatus.RESERVED)
                .orElseThrow(() -> new CouponValidationException("Không tìm thấy lượt giữ mã hoặc đã được xử lý"));

        // Check chưa expire
        if (reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CouponValidationException("Lượt giữ mã đã hết hạn");
        }

        // Đánh dấu REDEEMED
        reservation.setStatus(ReservationStatus.REDEEMED);
        reservation.setOrderId(request.getOrderId());
        reservationRepository.save(reservation);

        // Ghi nhận usage
        CouponUsage usage = CouponUsage.builder()
                .coupon(reservation.getCoupon())
                .customerId(reservation.getCustomerId())
                .orderId(request.getOrderId())
                .build();
        usageRepository.save(usage);

        // Nếu single-use coupon, đánh dấu USED
        Coupon coupon = reservation.getCoupon();
        if (coupon.getMaxUses() != null && coupon.getCurrentUses() >= coupon.getMaxUses()) {
            coupon.setStatus(CouponStatus.USED);
            couponRepository.save(coupon);
        }

        log.info("Coupon redeemed: reservationId={}, orderId={}", request.getReservationId(), request.getOrderId());
    }

    // =================== Release ===================

    @Override
    @Transactional
    public void releaseCoupon(Long reservationId) {
        CouponReservation reservation = reservationRepository
                .findByIdAndStatus(reservationId, ReservationStatus.RESERVED)
                .orElseThrow(() -> new CouponValidationException("Không tìm thấy lượt giữ mã hoặc đã được xử lý"));

        reservation.setStatus(ReservationStatus.RELEASED);
        reservationRepository.save(reservation);

        // Trả lại usage count atomic (tránh race condition)
        couponRepository.decrementCurrentUses(reservation.getCoupon().getId());

        log.info("Coupon released: reservationId={}", reservationId);
    }
}
