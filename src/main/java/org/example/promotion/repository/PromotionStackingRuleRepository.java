package org.example.promotion.repository;

import org.example.promotion.entity.PromotionStackingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionStackingRuleRepository extends JpaRepository<PromotionStackingRule, Long> {

    Optional<PromotionStackingRule> findByPromotionId(Long promotionId);
}
