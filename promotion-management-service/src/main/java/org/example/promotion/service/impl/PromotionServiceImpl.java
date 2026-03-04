package org.example.promotion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.promotion.dto.request.*;
import org.example.promotion.dto.response.*;
import org.example.promotion.entity.*;
import org.example.promotion.enums.PromotionStatus;
import org.example.promotion.exception.InvalidStatusException;
import org.example.promotion.exception.ResourceNotFoundException;
import org.example.promotion.mapper.PromotionMapper;
import org.example.promotion.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.promotion.service.IAuditLogService;
import org.example.promotion.service.IPromotionService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionServiceImpl implements IPromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionStackingRuleRepository stackingRuleRepository;
    private final IAuditLogService auditLogService;
    private final PromotionMapper mapper;

    // =================== Promotion CRUD ===================

    @Override
    @Transactional
    public PromotionResponse createPromotion(CreatePromotionRequest request) {
        Promotion promotion = mapper.toEntity(request);
        promotion = promotionRepository.save(promotion);

        auditLogService.saveAuditLog(promotion.getId(), "CREATE", null);
        log.info("Created promotion: id={}, name={}", promotion.getId(), promotion.getName());

        return mapper.toResponse(promotion);
    }

    @Override
    public PageResponse<PromotionResponse> getPromotions(
            PromotionStatus status, String search,
            LocalDateTime startDateFrom, LocalDateTime startDateTo,
            Pageable pageable) {

        Page<Promotion> page = promotionRepository.findAllWithFilters(
                status, search, startDateFrom, startDateTo, pageable);

        List<PromotionResponse> content = page.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return mapper.toPageResponse(page, content);
    }

    @Override
    public PromotionResponse getPromotionById(Long id) {
        Promotion promotion = findPromotionOrThrow(id);
        return mapper.toDetailResponse(promotion);
    }

    @Override
    @Transactional
    public PromotionResponse updatePromotion(Long id, UpdatePromotionRequest request) {
        Promotion promotion = findPromotionOrThrow(id);

        // Chỉ được sửa DRAFT hoặc ACTIVE
        if (promotion.getStatus() == PromotionStatus.EXPIRED) {
            throw new InvalidStatusException("Không thể cập nhật chương trình khuyến mãi đã hết hạn");
        }
        if (promotion.getStatus() == PromotionStatus.DISABLED) {
            throw new InvalidStatusException("Không thể cập nhật chương trình khuyến mãi đã bị vô hiệu hóa");
        }

        Map<String, Object> changes = new HashMap<>();

        if (request.getName() != null) {
            changes.put("name", Map.of("oldValue", promotion.getName(), "newValue", request.getName()));
            promotion.setName(request.getName());
        }
        if (request.getDescription() != null) {
            promotion.setDescription(request.getDescription());
        }
        if (request.getDiscountValue() != null) {
            changes.put("discountValue",
                    Map.of("oldValue", promotion.getDiscountValue(), "newValue", request.getDiscountValue()));
            promotion.setDiscountValue(request.getDiscountValue());
        }
        if (request.getMaxDiscountAmount() != null) {
            promotion.setMaxDiscountAmount(request.getMaxDiscountAmount());
        }
        if (request.getMinOrderValue() != null) {
            promotion.setMinOrderValue(request.getMinOrderValue());
        }
        if (request.getStartDate() != null) {
            promotion.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            changes.put("endDate",
                    Map.of("oldValue", promotion.getEndDate().toString(), "newValue", request.getEndDate().toString()));
            promotion.setEndDate(request.getEndDate());
        }
        if (request.getUsageLimit() != null) {
            promotion.setUsageLimit(request.getUsageLimit());
        }
        if (request.getUsageLimitPerCustomer() != null) {
            promotion.setUsageLimitPerCustomer(request.getUsageLimitPerCustomer());
        }

        promotion = promotionRepository.save(promotion);

        if (!changes.isEmpty()) {
            auditLogService.saveAuditLog(promotion.getId(), "UPDATE", changes);
        }
        log.info("Updated promotion: id={}", promotion.getId());

        return mapper.toResponse(promotion);
    }

    @Override
    @Transactional
    public void deletePromotion(Long id) {
        Promotion promotion = findPromotionOrThrow(id);

        if (promotion.getStatus() != PromotionStatus.DRAFT) {
            throw new InvalidStatusException("Chỉ có thể xóa chương trình khuyến mãi ở trạng thái DRAFT");
        }

        // Soft delete
        promotion.setIsDeleted(true);
        promotionRepository.save(promotion);

        auditLogService.saveAuditLog(promotion.getId(), "DELETE", null);
        log.info("Soft deleted promotion: id={}", id);
    }

    // =================== Lifecycle ===================

    @Override
    @Transactional
    public PromotionResponse activatePromotion(Long id) {
        Promotion promotion = findPromotionOrThrow(id);

        if (promotion.getStatus() != PromotionStatus.DRAFT) {
            throw new InvalidStatusException("Chỉ có thể kích hoạt chương trình khuyến mãi ở trạng thái DRAFT");
        }

        // Validate có rules
        if (promotion.getRules() == null || promotion.getRules().isEmpty()) {
            throw new InvalidStatusException("Chương trình khuyến mãi phải có ít nhất một quy tắc trước khi kích hoạt");
        }

        promotion.setStatus(PromotionStatus.ACTIVE);
        promotion = promotionRepository.save(promotion);

        auditLogService.saveAuditLog(promotion.getId(), "ACTIVATE", null);
        log.info("Activated promotion: id={}", id);

        return mapper.toResponse(promotion);
    }

    @Override
    @Transactional
    public PromotionResponse disablePromotion(Long id, DisablePromotionRequest request) {
        Promotion promotion = findPromotionOrThrow(id);

        if (promotion.getStatus() != PromotionStatus.ACTIVE) {
            throw new InvalidStatusException("Chỉ có thể vô hiệu hóa chương trình khuyến mãi ở trạng thái ACTIVE");
        }

        promotion.setStatus(PromotionStatus.DISABLED);
        promotion = promotionRepository.save(promotion);

        Map<String, Object> changes = new HashMap<>();
        if (request != null && request.getReason() != null) {
            changes.put("disableReason", request.getReason());
        }
        auditLogService.saveAuditLog(promotion.getId(), "DISABLE", changes.isEmpty() ? null : changes);
        log.info("Disabled promotion: id={}, reason={}", id,
                request != null ? request.getReason() : "N/A");

        return mapper.toResponse(promotion);
    }

    @Override
    @Transactional
    public StackingConfigResponse updateStackingConfig(Long promotionId, StackingConfigRequest request) {
        Promotion promotion = findPromotionOrThrow(promotionId);

        PromotionStackingRule stackingRule = stackingRuleRepository
                .findByPromotionId(promotionId)
                .orElse(PromotionStackingRule.builder().promotion(promotion).build());

        if (request.getStackable() != null)
            stackingRule.setStackable(request.getStackable());
        if (request.getPriority() != null)
            stackingRule.setPriority(request.getPriority());
        if (request.getExclusiveGroupId() != null)
            stackingRule.setExclusiveGroup(request.getExclusiveGroupId());
        if (request.getMaxStackDiscount() != null)
            stackingRule.setMaxStackDiscount(request.getMaxStackDiscount());

        stackingRule = stackingRuleRepository.save(stackingRule);
        log.info("Updated stacking config: promotionId={}", promotionId);

        return mapper.toStackingResponse(stackingRule);
    }

    private Promotion findPromotionOrThrow(Long id) {
        return promotionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
    }
}
