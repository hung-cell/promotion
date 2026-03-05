package org.example.promotion.management.repository;

import org.example.promotion.management.entity.PromotionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRuleRepository extends JpaRepository<PromotionRule, Long> {

    List<PromotionRule> findByPromotionId(Long promotionId);
}
