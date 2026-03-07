package org.example.promotion.management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.promotion.common.enums.RuleType;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "promotion_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PromotionRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RuleType type;

    // For PERCENTAGE_DISCOUNT
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "max_discount_amount", precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;

    // For FIXED_DISCOUNT
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "min_order_value", precision = 15, scale = 2)
    private BigDecimal minOrderValue;

    // For BUY_X_GET_Y
    @Column(name = "buy_product_id", length = 100)
    private String buyProductId;

    @Column(name = "buy_quantity")
    private Integer buyQuantity;

    @Column(name = "get_product_id", length = 100)
    private String getProductId;

    @Column(name = "get_quantity")
    private Integer getQuantity;

    @Column(name = "max_applications")
    private Integer maxApplications;

    // For FREE_SHIPPING
    @Column(name = "applicable_regions", length = 500)
    private String applicableRegions; // JSON array stored as string

    @Column(name = "applicable_shipping_methods", length = 500)
    private String applicableShippingMethods; // JSON array stored as string

}
