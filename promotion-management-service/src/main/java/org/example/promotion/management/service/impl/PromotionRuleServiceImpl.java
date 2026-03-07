package org.example.promotion.management.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.promotion.management.dto.request.CreateRuleRequest;
import org.example.promotion.management.dto.response.RuleResponse;
import org.example.promotion.management.entity.Promotion;
import org.example.promotion.management.entity.PromotionRule;
import org.example.promotion.management.exception.ResourceNotFoundException;
import org.example.promotion.management.mapper.PromotionMapper;
import org.example.promotion.management.repository.PromotionRepository;
import org.example.promotion.management.repository.PromotionRuleRepository;
import org.example.promotion.management.service.IPromotionRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionRuleServiceImpl implements IPromotionRuleService {

    private final PromotionRuleRepository ruleRepository;
    private final PromotionRepository promotionRepository;
    private final PromotionMapper mapper;

    @Override
    @Transactional
    public RuleResponse addRule(Long promotionId, CreateRuleRequest request) {
        Promotion promotion = findPromotionOrThrow(promotionId);
        PromotionRule rule = mapper.toRuleEntity(request, promotion);
        rule = ruleRepository.save(rule);

        log.info("Added rule to promotion: promotionId={}, ruleId={}", promotionId, rule.getId());
        return mapper.toRuleResponse(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleResponse> getRules(Long promotionId) {
        findPromotionOrThrow(promotionId); // validate exists
        return ruleRepository.findByPromotionId(promotionId).stream()
                .map(mapper::toRuleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRule(Long promotionId, Long ruleId) {
        findPromotionOrThrow(promotionId);
        PromotionRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rule", "id", ruleId));

        if (!rule.getPromotion().getId().equals(promotionId)) {
            throw new ResourceNotFoundException("Rule", "id", ruleId);
        }

        ruleRepository.delete(rule);
        log.info("Deleted rule: promotionId={}, ruleId={}", promotionId, ruleId);
    }

    private Promotion findPromotionOrThrow(Long id) {
        return promotionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
    }
}
