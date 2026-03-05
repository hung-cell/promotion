package org.example.promotion.management.repository;

import org.example.promotion.management.entity.PromotionStackingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionStackingRuleRepository extends JpaRepository<PromotionStackingRule, Long> {

    Optional<PromotionStackingRule> findByPromotionId(Long promotionId);
}
