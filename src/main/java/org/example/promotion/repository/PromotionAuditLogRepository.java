package org.example.promotion.repository;

import org.example.promotion.entity.PromotionAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PromotionAuditLogRepository extends JpaRepository<PromotionAuditLog, Long> {

        @Query("SELECT a FROM PromotionAuditLog a WHERE a.promotionId = :promotionId " +
                        "AND (:action IS NULL OR a.action = :action) " +
                        "AND (:fromDate IS NULL OR a.createdAt >= :fromDate) " +
                        "AND (:toDate IS NULL OR a.createdAt <= :toDate)")
        Page<PromotionAuditLog> findByPromotionIdWithFilters(
                        @Param("promotionId") Long promotionId,
                        @Param("action") String action,
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate,
                        Pageable pageable);
}
