package org.example.promotion.repository;

import org.example.promotion.entity.CouponReservation;
import org.example.promotion.enums.ReservationStatus;
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
