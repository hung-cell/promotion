package org.example.promotion.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.promotion.enums.ReservationStatus;
import org.example.promotion.repository.CouponRepository;
import org.example.promotion.repository.CouponReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponReservationScheduler {

    private final CouponReservationRepository reservationRepository;
    private final CouponRepository couponRepository;

    @Scheduled(fixedRateString = "${coupon.reservation.scheduler.release-rate:60000}")
    @Transactional
    public void releaseExpiredReservations() {
        log.info("Running scheduled job to release expired coupon reservations at {}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();
        // Since we need to restore usage count line by line, a simple query is not
        // enough
        // Ideally we would fetch them and release them logic by logic using the service
        // For simplicity we will assume the service's release method handles one, so we
        // fetch expired ones here

        var expiredReservations = reservationRepository.findByStatusAndExpiresAtBefore(ReservationStatus.RESERVED, now);

        if (!expiredReservations.isEmpty()) {
            for (var reservation : expiredReservations) {
                try {
                    reservation.setStatus(ReservationStatus.EXPIRED);
                    reservationRepository.save(reservation);

                    // atomic decrement currentUses
                    couponRepository.decrementCurrentUses(reservation.getCoupon().getId());
                } catch (Exception e) {
                    log.error("Failed to release reservation id: {}", reservation.getId(), e);
                }
            }
            log.info("Released {} expired coupon reservations", expiredReservations.size());
        }
    }
}
