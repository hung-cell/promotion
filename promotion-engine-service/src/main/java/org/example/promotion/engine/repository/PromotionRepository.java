package org.example.promotion.engine.repository;

import org.example.promotion.engine.entity.Promotion;
import org.example.promotion.common.enums.PromotionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false " +
            "AND (:status IS NULL OR p.status = :status)")
    Page<Promotion> findAllWithFilters(@Param("status") PromotionStatus status, Pageable pageable);
}
