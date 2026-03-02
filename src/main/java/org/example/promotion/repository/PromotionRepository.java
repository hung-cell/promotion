package org.example.promotion.repository;

import org.example.promotion.entity.Promotion;
import org.example.promotion.enums.PromotionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

        Optional<Promotion> findByIdAndIsDeletedFalse(Long id);

        @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false " +
                        "AND (:status IS NULL OR p.status = :status) " +
                        "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:startDateFrom IS NULL OR p.startDate >= :startDateFrom) " +
                        "AND (:startDateTo IS NULL OR p.startDate <= :startDateTo)")
        Page<Promotion> findAllWithFilters(
                        @Param("status") PromotionStatus status,
                        @Param("search") String search,
                        @Param("startDateFrom") LocalDateTime startDateFrom,
                        @Param("startDateTo") LocalDateTime startDateTo,
                        Pageable pageable);

        @Modifying
        @Query("UPDATE Promotion p SET p.status = :newStatus WHERE p.status = :currentStatus AND p.endDate < :now AND p.isDeleted = false")
        int expirePromotions(@Param("now") LocalDateTime now, @Param("currentStatus") PromotionStatus currentStatus,
                        @Param("newStatus") PromotionStatus newStatus);
}
