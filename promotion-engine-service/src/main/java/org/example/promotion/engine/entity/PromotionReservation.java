package org.example.promotion.engine.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.promotion.common.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tracks a reservation of promotion quota.
 * A reservation is created when a customer starts checkout.
 * It is converted to PromotionUsage records on commit,
 * or deleted on release/expiry.
 */
@Entity
@Table(name = "promotion_reservations", indexes = {
        @Index(name = "idx_reservation_order", columnList = "order_id"),
        @Index(name = "idx_reservation_customer", columnList = "customer_id"),
        @Index(name = "idx_reservation_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PromotionReservation extends BaseEntity {

    @Column(name = "reservation_id", nullable = false, unique = true, length = 100)
    private String reservationId;

    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    // JSON array of promotion IDs
    @Column(name = "promotion_ids", columnDefinition = "TEXT")
    private String promotionIds;

    // JSON array of coupon codes
    @Column(name = "coupon_codes", columnDefinition = "TEXT")
    private String couponCodes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "committed_at")
    private LocalDateTime committedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "release_reason", length = 500)
    private String releaseReason;
}
