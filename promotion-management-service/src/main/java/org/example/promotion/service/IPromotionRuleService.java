package org.example.promotion.service;

import org.example.promotion.dto.request.CreateRuleRequest;
import org.example.promotion.dto.response.RuleResponse;

import java.util.List;

public interface IPromotionRuleService {
    RuleResponse addRule(Long promotionId, CreateRuleRequest request);

    List<RuleResponse> getRules(Long promotionId);

    void deleteRule(Long promotionId, Long ruleId);
}
