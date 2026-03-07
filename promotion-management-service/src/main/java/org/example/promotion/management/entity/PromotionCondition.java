package org.example.promotion.management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.promotion.common.enums.ConditionType;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "promotion_conditions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PromotionCondition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 50)
    private ConditionType conditionType;

    // Lưu JSON linh hoạt cho từng loại condition
    @Column(name = "condition_value", columnDefinition = "TEXT")
    private String conditionValue; // JSON string

    @Column(name = "operator", length = 20)
    private String operator; // IN, NOT_IN

}
