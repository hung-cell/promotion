package org.example.promotion.service;

import org.example.promotion.dto.request.CreateConditionRequest;
import org.example.promotion.dto.response.ConditionResponse;

import java.util.List;

public interface IPromotionConditionService {
    ConditionResponse addCondition(Long promotionId, CreateConditionRequest request);

    List<ConditionResponse> getConditions(Long promotionId);

    void deleteCondition(Long promotionId, Long conditionId);
}
