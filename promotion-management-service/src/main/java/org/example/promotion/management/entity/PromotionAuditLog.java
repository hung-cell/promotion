package org.example.promotion.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "promotion_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PromotionAuditLog extends BaseEntity {

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    @Column(nullable = false, length = 50)
    private String action; // CREATE, UPDATE, DELETE, ACTIVATE, DISABLE

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "user_name", length = 255)
    private String userName;

    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes; // JSON: {"field": {"oldValue": "...", "newValue": "..."}}

}
