package org.example.promotion.engine.repository;

import org.example.promotion.engine.entity.PromotionReservation;
import org.example.promotion.common.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionReservationRepository extends JpaRepository<PromotionReservation, Long> {

    Optional<PromotionReservation> findByReservationId(String reservationId);

    Optional<PromotionReservation> findByOrderIdAndStatus(String orderId, ReservationStatus status);
}
