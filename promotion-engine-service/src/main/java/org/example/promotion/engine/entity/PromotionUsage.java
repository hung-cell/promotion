package org.example.promotion.engine.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Records a confirmed usage of a promotion after order is placed (committed).
 */
@Entity
@Table(name = "promotion_usages", indexes = {
        @Index(name = "idx_usage_promotion", columnList = "promotion_id"),
        @Index(name = "idx_usage_customer", columnList = "customer_id"),
        @Index(name = "idx_usage_order", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PromotionUsage extends BaseEntity {

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "reservation_id", length = 100)
    private String reservationId;
}
