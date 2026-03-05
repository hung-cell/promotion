package org.example.promotion.management.service;

import org.example.promotion.management.dto.request.CreateConditionRequest;
import org.example.promotion.management.dto.response.ConditionResponse;

import java.util.List;

public interface IPromotionConditionService {
    ConditionResponse addCondition(Long promotionId, CreateConditionRequest request);

    List<ConditionResponse> getConditions(Long promotionId);

    void deleteCondition(Long promotionId, Long conditionId);
}
