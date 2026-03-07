package org.example.promotion.management.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "promotion_stacking_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PromotionStackingRule extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false, unique = true)
    private Promotion promotion;

    @Column(name = "stackable")
    @Builder.Default
    private Boolean stackable = false;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "exclusive_group", length = 100)
    private String exclusiveGroup;

    @Column(name = "max_stack_discount", precision = 15, scale = 2)
    private BigDecimal maxStackDiscount;
}
