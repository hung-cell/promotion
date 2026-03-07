package org.example.promotion.management.service;

import org.example.promotion.management.dto.request.CreatePromotionRequest;
import org.example.promotion.management.dto.request.DisablePromotionRequest;
import org.example.promotion.management.dto.request.StackingConfigRequest;
import org.example.promotion.management.dto.request.UpdatePromotionRequest;
import org.example.promotion.common.dto.response.PageResponse;
import org.example.promotion.management.dto.response.PromotionResponse;
import org.example.promotion.management.dto.response.StackingConfigResponse;
import org.example.promotion.common.enums.PromotionStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface IPromotionService {
    PromotionResponse createPromotion(CreatePromotionRequest request);

    PageResponse<PromotionResponse> getPromotions(PromotionStatus status, String search, LocalDateTime startDateFrom,
            LocalDateTime startDateTo, Pageable pageable);

    PromotionResponse getPromotionById(Long id);

    PromotionResponse updatePromotion(Long id, UpdatePromotionRequest request);

    void deletePromotion(Long id);

    PromotionResponse activatePromotion(Long id);

    PromotionResponse disablePromotion(Long id, DisablePromotionRequest request);

    StackingConfigResponse updateStackingConfig(Long promotionId, StackingConfigRequest request);
}
