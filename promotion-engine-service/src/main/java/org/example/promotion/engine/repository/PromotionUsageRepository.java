package org.example.promotion.engine.repository;

import org.example.promotion.engine.entity.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {

    List<PromotionUsage> findByOrderId(String orderId);

    List<PromotionUsage> findByCustomerIdAndPromotionId(String customerId, Long promotionId);

    long countByCustomerIdAndPromotionId(String customerId, Long promotionId);
}
