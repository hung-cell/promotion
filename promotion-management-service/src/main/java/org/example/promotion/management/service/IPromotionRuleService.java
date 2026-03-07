package org.example.promotion.management.service;

import org.example.promotion.management.dto.request.CreateRuleRequest;
import org.example.promotion.management.dto.response.RuleResponse;

import java.util.List;

public interface IPromotionRuleService {
    RuleResponse addRule(Long promotionId, CreateRuleRequest request);

    List<RuleResponse> getRules(Long promotionId);

    void deleteRule(Long promotionId, Long ruleId);
}
