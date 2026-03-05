package org.example.promotion.management.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.promotion.management.dto.request.CreateConditionRequest;
import org.example.promotion.management.dto.request.CreatePromotionRequest;
import org.example.promotion.management.dto.request.CreateRuleRequest;
import org.example.promotion.management.dto.response.*;
import org.example.promotion.common.dto.response.ApiResponse;
import org.example.promotion.common.dto.response.PageResponse;
import org.example.promotion.common.dto.response.PaginationInfo;
import org.example.promotion.management.entity.*;
import org.example.promotion.common.enums.PromotionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PromotionMapper {

    private final ObjectMapper objectMapper;

    // ====== Promotion ======

    public Promotion toEntity(CreatePromotionRequest request) {
        return Promotion.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderValue(request.getMinOrderValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .usageLimit(request.getUsageLimit())
                .usageLimitPerCustomer(request.getUsageLimitPerCustomer())
                .status(PromotionStatus.DRAFT)
                .build();
    }

    public PromotionResponse toResponse(Promotion entity) {
        return PromotionResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .discountValue(entity.getDiscountValue())
                .maxDiscountAmount(entity.getMaxDiscountAmount())
                .minOrderValue(entity.getMinOrderValue())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .usageLimit(entity.getUsageLimit())
                .usageLimitPerCustomer(entity.getUsageLimitPerCustomer())
                .usageCount(entity.getUsageCount())
                .status(entity.getStatus())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public PromotionResponse toDetailResponse(Promotion entity) {
        PromotionResponse response = toResponse(entity);
        if (entity.getRules() != null) {
            response.setRules(entity.getRules().stream()
                    .map(this::toRuleResponse)
                    .collect(Collectors.toList()));
        }
        if (entity.getConditions() != null) {
            response.setConditions(entity.getConditions().stream()
                    .map(this::toConditionResponse)
                    .collect(Collectors.toList()));
        }
        if (entity.getStackingRule() != null) {
            response.setStackingConfig(toStackingResponse(entity.getStackingRule()));
        }
        return response;
    }

    // ====== Rule ======

    public PromotionRule toRuleEntity(CreateRuleRequest request, Promotion promotion) {
        PromotionRule rule = PromotionRule.builder()
                .promotion(promotion)
                .type(request.getType())
                .discountPercent(request.getDiscountPercent())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .discountAmount(request.getDiscountAmount())
                .minOrderValue(request.getMinOrderValue())
                .buyProductId(request.getBuyProductId())
                .buyQuantity(request.getBuyQuantity())
                .getProductId(request.getGetProductId())
                .getQuantity(request.getGetQuantity())
                .maxApplications(request.getMaxApplications())
                .build();

        if (request.getApplicableRegions() != null) {
            rule.setApplicableRegions(toJson(request.getApplicableRegions()));
        }
        if (request.getApplicableShippingMethods() != null) {
            rule.setApplicableShippingMethods(toJson(request.getApplicableShippingMethods()));
        }

        return rule;
    }

    public RuleResponse toRuleResponse(PromotionRule entity) {
        RuleResponse.RuleResponseBuilder builder = RuleResponse.builder()
                .id(entity.getId())
                .promotionId(entity.getPromotion().getId())
                .type(entity.getType())
                .discountPercent(entity.getDiscountPercent())
                .maxDiscountAmount(entity.getMaxDiscountAmount())
                .discountAmount(entity.getDiscountAmount())
                .minOrderValue(entity.getMinOrderValue())
                .buyProductId(entity.getBuyProductId())
                .buyQuantity(entity.getBuyQuantity())
                .getProductId(entity.getGetProductId())
                .getQuantity(entity.getGetQuantity())
                .maxApplications(entity.getMaxApplications())
                .createdAt(entity.getCreatedAt());

        if (entity.getApplicableRegions() != null) {
            builder.applicableRegions(fromJson(entity.getApplicableRegions()));
        }
        if (entity.getApplicableShippingMethods() != null) {
            builder.applicableShippingMethods(fromJson(entity.getApplicableShippingMethods()));
        }

        return builder.build();
    }

    // ====== Condition ======

    public PromotionCondition toConditionEntity(CreateConditionRequest request, Promotion promotion) {
        Map<String, Object> valueMap = new HashMap<>();

        switch (request.getType()) {
            case PRODUCT:
                if (request.getIncludeSkus() != null)
                    valueMap.put("includeSkus", request.getIncludeSkus());
                if (request.getIncludeCategoryIds() != null)
                    valueMap.put("includeCategoryIds", request.getIncludeCategoryIds());
                if (request.getIncludeBrandIds() != null)
                    valueMap.put("includeBrandIds", request.getIncludeBrandIds());
                if (request.getExcludeSkus() != null)
                    valueMap.put("excludeSkus", request.getExcludeSkus());
                break;
            case CUSTOMER_SEGMENT:
                if (request.getCustomerSegments() != null)
                    valueMap.put("customerSegments", request.getCustomerSegments());
                if (request.getCustomerTiers() != null)
                    valueMap.put("customerTiers", request.getCustomerTiers());
                if (request.getFirstPurchaseOnly() != null)
                    valueMap.put("firstPurchaseOnly", request.getFirstPurchaseOnly());
                break;
            case ORDER:
                if (request.getMinOrderValue() != null)
                    valueMap.put("minOrderValue", request.getMinOrderValue());
                if (request.getMinQuantity() != null)
                    valueMap.put("minQuantity", request.getMinQuantity());
                if (request.getMaxUsagePerCustomer() != null)
                    valueMap.put("maxUsagePerCustomer", request.getMaxUsagePerCustomer());
                if (request.getMaxTotalUsage() != null)
                    valueMap.put("maxTotalUsage", request.getMaxTotalUsage());
                break;
            case CHANNEL:
                if (request.getIncludeChannels() != null)
                    valueMap.put("includeChannels", request.getIncludeChannels());
                if (request.getExcludeChannels() != null)
                    valueMap.put("excludeChannels", request.getExcludeChannels());
                break;
        }

        return PromotionCondition.builder()
                .promotion(promotion)
                .conditionType(request.getType())
                .conditionValue(toJson(valueMap))
                .build();
    }

    public ConditionResponse toConditionResponse(PromotionCondition entity) {
        Object parsedValue = null;
        try {
            parsedValue = objectMapper.readValue(entity.getConditionValue(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            parsedValue = entity.getConditionValue();
        }

        return ConditionResponse.builder()
                .id(entity.getId())
                .promotionId(entity.getPromotion().getId())
                .conditionType(entity.getConditionType())
                .conditionValue(parsedValue)
                .operator(entity.getOperator())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // ====== Stacking ======

    public StackingConfigResponse toStackingResponse(PromotionStackingRule entity) {
        return StackingConfigResponse.builder()
                .promotionId(entity.getPromotion().getId())
                .stackable(entity.getStackable())
                .priority(entity.getPriority())
                .exclusiveGroupId(entity.getExclusiveGroup())
                .maxStackDiscount(entity.getMaxStackDiscount())
                .build();
    }

    // ====== Audit Log ======

    public AuditLogResponse toAuditLogResponse(PromotionAuditLog entity) {
        Map<String, Object> changes = null;
        if (entity.getChanges() != null) {
            try {
                changes = objectMapper.readValue(entity.getChanges(), new TypeReference<Map<String, Object>>() {
                });
            } catch (Exception e) {
                // ignore parse error
            }
        }

        return AuditLogResponse.builder()
                .id(entity.getId())
                .promotionId(entity.getPromotionId())
                .action(entity.getAction())
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .timestamp(entity.getCreatedAt())
                .changes(changes)
                .build();
    }

    // ====== Pagination Helper ======

    public <T> PageResponse<T> toPageResponse(Page<?> page, List<T> content) {
        PaginationInfo pagination = PaginationInfo.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();

        return PageResponse.<T>builder()
                .content(content)
                .pagination(pagination)
                .build();
    }

    // ====== JSON Helpers ======

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<String> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}
