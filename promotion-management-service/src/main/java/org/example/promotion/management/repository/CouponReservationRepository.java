package org.example.promotion.management.repository;

import org.example.promotion.management.entity.CouponReservation;
import org.example.promotion.common.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponReservationRepository extends JpaRepository<CouponReservation, Long> {

    Optional<CouponReservation> findByIdAndStatus(Long id, ReservationStatus status);

    List<CouponReservation> findByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime dateTime);

    long countByCouponIdAndCustomerIdAndStatus(Long couponId, String customerId, ReservationStatus status);
}
