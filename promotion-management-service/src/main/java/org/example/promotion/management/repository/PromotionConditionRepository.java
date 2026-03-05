package org.example.promotion.management.repository;

import org.example.promotion.management.entity.PromotionCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionConditionRepository extends JpaRepository<PromotionCondition, Long> {

    List<PromotionCondition> findByPromotionId(Long promotionId);
}
