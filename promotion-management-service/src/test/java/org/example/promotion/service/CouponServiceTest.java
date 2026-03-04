package org.example.promotion.service;

import org.example.promotion.dto.request.RedeemCouponRequest;
import org.example.promotion.dto.request.ReserveCouponRequest;
import org.example.promotion.dto.request.ValidateCouponRequest;
import org.example.promotion.dto.response.CouponReservationResponse;
import org.example.promotion.entity.Coupon;
import org.example.promotion.entity.CouponReservation;
import org.example.promotion.entity.Promotion;
import org.example.promotion.enums.CouponStatus;
import org.example.promotion.enums.PromotionStatus;
import org.example.promotion.enums.PromotionType;
import org.example.promotion.enums.ReservationStatus;
import org.example.promotion.exception.CouponValidationException;
import org.example.promotion.repository.CouponRepository;
import org.example.promotion.repository.CouponReservationRepository;
import org.example.promotion.repository.CouponUsageRepository;
import org.example.promotion.repository.PromotionRepository;
import org.example.promotion.service.impl.CouponServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @Mock
    private CouponReservationRepository reservationRepository;

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Promotion promotion;
    private Coupon coupon;
    private CouponReservation reservation;

    @BeforeEach
    void setUp() {
        promotion = new Promotion();
        promotion.setId(1L);
        promotion.setStatus(PromotionStatus.ACTIVE);
        promotion.setStartDate(LocalDateTime.now().minusDays(1));
        promotion.setEndDate(LocalDateTime.now().plusDays(1));
        promotion.setMinOrderValue(new BigDecimal("100.00"));
        promotion.setType(PromotionType.PERCENTAGE);
        promotion.setDiscountValue(new BigDecimal("10"));

        coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCode("SUMMER10");
        coupon.setPromotion(promotion);
        coupon.setCurrentUses(0);
        coupon.setMaxUses(10);
        coupon.setStatus(CouponStatus.ACTIVE);

        reservation = new CouponReservation();
        reservation.setId(1L);
        reservation.setCoupon(coupon);
        reservation.setCustomerId("CUST-1");
        reservation.setOrderId("ORDER-1");
        reservation.setStatus(ReservationStatus.RESERVED);
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(10));
    }

    @Test
    void validateCoupon_Success() {
        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode("SUMMER10");
        request.setOrderAmount(new BigDecimal("150.00"));

        when(couponRepository.findByCode("SUMMER10")).thenReturn(Optional.of(coupon));

        assertDoesNotThrow(() -> couponService.validateCoupon(request));
        verify(couponRepository).findByCode("SUMMER10");
    }

    @Test
    void validateCoupon_NotFound_ThrowsException() {
        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode("INVALID");
        request.setOrderAmount(new BigDecimal("150.00"));

        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        CouponValidationException ex = assertThrows(CouponValidationException.class,
                () -> couponService.validateCoupon(request));
        assertEquals("Không tìm thấy mã giảm giá: INVALID", ex.getMessage());
    }

    @Test
    void validateCoupon_Expired_ThrowsException() {
        coupon.setExpiryDate(LocalDateTime.now().minusDays(1));
        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode("SUMMER10");
        request.setOrderAmount(new BigDecimal("150.00"));

        when(couponRepository.findByCode("SUMMER10")).thenReturn(Optional.of(coupon));

        CouponValidationException ex = assertThrows(CouponValidationException.class,
                () -> couponService.validateCoupon(request));
        assertEquals("Mã giảm giá đã hết hạn sử dụng", ex.getMessage());
    }

    @Test
    void validateCoupon_LimitReached_ThrowsException() {
        coupon.setCurrentUses(10);
        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode("SUMMER10");
        request.setOrderAmount(new BigDecimal("150.00"));

        when(couponRepository.findByCode("SUMMER10")).thenReturn(Optional.of(coupon));

        CouponValidationException ex = assertThrows(CouponValidationException.class,
                () -> couponService.validateCoupon(request));
        assertEquals("Mã giảm giá đã hết lượt sử dụng", ex.getMessage());
    }

    @Test
    void reserveCoupon_Success() {
        ReserveCouponRequest request = new ReserveCouponRequest();
        request.setCode("SUMMER10");
        request.setCustomerId("CUST-1");
        request.setOrderId("ORDER-1");

        when(couponRepository.findByCode("SUMMER10")).thenReturn(Optional.of(coupon));
        when(reservationRepository.countByCouponIdAndCustomerIdAndStatus(coupon.getId(), "CUST-1",
                ReservationStatus.RESERVED)).thenReturn(0L);
        when(reservationRepository.save(any(CouponReservation.class))).thenReturn(reservation);
        when(couponRepository.incrementCurrentUses(coupon.getId(), 1)).thenReturn(1);

        CouponReservationResponse response = couponService.reserveCoupon(request);

        assertNotNull(response);
        assertEquals(ReservationStatus.RESERVED.name(), response.getStatus());
        verify(reservationRepository).save(any(CouponReservation.class));
        verify(couponRepository).incrementCurrentUses(coupon.getId(), 1);
    }

    @Test
    void reserveCoupon_ActiveReservationExists_ThrowsException() {
        ReserveCouponRequest request = new ReserveCouponRequest();
        request.setCode("SUMMER10");
        request.setCustomerId("CUST-1");

        when(couponRepository.findByCode("SUMMER10")).thenReturn(Optional.of(coupon));
        when(reservationRepository.countByCouponIdAndCustomerIdAndStatus(coupon.getId(), "CUST-1",
                ReservationStatus.RESERVED)).thenReturn(1L);

        CouponValidationException ex = assertThrows(CouponValidationException.class,
                () -> couponService.reserveCoupon(request));
        assertEquals("Bạn đã có một lượt giữ mã này đang hoạt động", ex.getMessage());
    }

    @Test
    void redeemCoupon_Success() {
        RedeemCouponRequest request = new RedeemCouponRequest();
        request.setReservationId(1L);
        request.setOrderId("ORDER-1");

        when(reservationRepository.findByIdAndStatus(1L, ReservationStatus.RESERVED))
                .thenReturn(Optional.of(reservation));

        assertDoesNotThrow(() -> couponService.redeemCoupon(request));

        verify(reservationRepository).save(reservation);
        assertEquals(ReservationStatus.REDEEMED, reservation.getStatus());
        verify(couponUsageRepository).save(any());
    }

    @Test
    void redeemCoupon_ExpiredReservation_ThrowsException() {
        reservation.setExpiresAt(LocalDateTime.now().minusMinutes(5));
        RedeemCouponRequest request = new RedeemCouponRequest();
        request.setReservationId(1L);
        request.setOrderId("ORDER-1");

        when(reservationRepository.findByIdAndStatus(1L, ReservationStatus.RESERVED))
                .thenReturn(Optional.of(reservation));

        CouponValidationException ex = assertThrows(CouponValidationException.class,
                () -> couponService.redeemCoupon(request));
        assertEquals("Lượt giữ mã đã hết hạn", ex.getMessage());
    }

    @Test
    void releaseCoupon_Success() {
        coupon.setCurrentUses(1);
        when(reservationRepository.findByIdAndStatus(1L, ReservationStatus.RESERVED))
                .thenReturn(Optional.of(reservation));
        when(couponRepository.decrementCurrentUses(coupon.getId())).thenReturn(1);

        assertDoesNotThrow(() -> couponService.releaseCoupon(1L));

        verify(reservationRepository).save(reservation);
        assertEquals(ReservationStatus.RELEASED, reservation.getStatus());
        verify(couponRepository).decrementCurrentUses(coupon.getId());
    }
}
