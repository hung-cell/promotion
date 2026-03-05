package org.example.promotion.management.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.promotion.management.dto.request.CreateConditionRequest;
import org.example.promotion.management.dto.response.ConditionResponse;
import org.example.promotion.management.entity.Promotion;
import org.example.promotion.management.entity.PromotionCondition;
import org.example.promotion.management.exception.ResourceNotFoundException;
import org.example.promotion.management.mapper.PromotionMapper;
import org.example.promotion.management.repository.PromotionConditionRepository;
import org.example.promotion.management.repository.PromotionRepository;
import org.example.promotion.management.service.IPromotionConditionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionConditionServiceImpl implements IPromotionConditionService {

    private final PromotionConditionRepository conditionRepository;
    private final PromotionRepository promotionRepository;
    private final PromotionMapper mapper;

    @Override
    @Transactional
    public ConditionResponse addCondition(Long promotionId, CreateConditionRequest request) {
        Promotion promotion = findPromotionOrThrow(promotionId);
        PromotionCondition condition = mapper.toConditionEntity(request, promotion);
        condition = conditionRepository.save(condition);

        log.info("Added condition to promotion: promotionId={}, conditionId={}", promotionId, condition.getId());
        return mapper.toConditionResponse(condition);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConditionResponse> getConditions(Long promotionId) {
        findPromotionOrThrow(promotionId);
        return conditionRepository.findByPromotionId(promotionId).stream()
                .map(mapper::toConditionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCondition(Long promotionId, Long conditionId) {
        findPromotionOrThrow(promotionId);
        PromotionCondition condition = conditionRepository.findById(conditionId)
                .orElseThrow(() -> new ResourceNotFoundException("Condition", "id", conditionId));

        if (!condition.getPromotion().getId().equals(promotionId)) {
            throw new ResourceNotFoundException("Condition", "id", conditionId);
        }

        conditionRepository.delete(condition);
        log.info("Deleted condition: promotionId={}, conditionId={}", promotionId, conditionId);
    }

    private Promotion findPromotionOrThrow(Long id) {
        return promotionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
    }
}
