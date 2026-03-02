package org.example.promotion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.promotion.common.AppConstants;
import org.example.promotion.dto.request.*;
import org.example.promotion.dto.response.*;
import org.example.promotion.enums.PromotionStatus;
import org.example.promotion.service.IAuditLogService;
import org.example.promotion.service.IPromotionConditionService;
import org.example.promotion.service.IPromotionRuleService;
import org.example.promotion.service.IPromotionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@Validated
public class PromotionController {

    private final IPromotionService promotionService;
    private final IPromotionRuleService ruleService;
    private final IPromotionConditionService conditionService;
    private final IAuditLogService auditLogService;

    // =================== CRUD ===================

    @PostMapping
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(
            @Valid @RequestBody CreatePromotionRequest request) {
        PromotionResponse response = promotionService.createPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Promotion created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PromotionResponse>>> getPromotions(
            @RequestParam(required = false) PromotionStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        String[] sortParts = sort.split(",");
        Sort sortObj = Sort.by(Sort.Direction.fromString(sortParts.length > 1 ? sortParts[1] : "desc"), sortParts[0]);
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sortObj);

        PageResponse<PromotionResponse> response = promotionService.getPromotions(
                status, search, startDateFrom, startDateTo, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionResponse>> getPromotionById(@PathVariable Long id) {
        PromotionResponse response = promotionService.getPromotionById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionResponse>> updatePromotion(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePromotionRequest request) {
        PromotionResponse response = promotionService.updatePromotion(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật chương trình khuyến mãi thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Promotion deleted successfully"));
    }

    // =================== Lifecycle ===================

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<PromotionResponse>> activatePromotion(@PathVariable Long id) {
        PromotionResponse response = promotionService.activatePromotion(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Promotion activated successfully"));
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<PromotionResponse>> disablePromotion(
            @PathVariable Long id,
            @RequestBody(required = false) DisablePromotionRequest request) {
        PromotionResponse response = promotionService.disablePromotion(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Promotion disabled successfully"));
    }

    // =================== Rules ===================

    @PostMapping("/{id}/rules")
    public ResponseEntity<ApiResponse<RuleResponse>> addRule(
            @PathVariable Long id,
            @Valid @RequestBody CreateRuleRequest request) {
        RuleResponse response = ruleService.addRule(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Rule added successfully"));
    }

    @GetMapping("/{id}/rules")
    public ResponseEntity<ApiResponse<List<RuleResponse>>> getRules(@PathVariable Long id) {
        List<RuleResponse> response = ruleService.getRules(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}/rules/{ruleId}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(
            @PathVariable Long id,
            @PathVariable Long ruleId) {
        ruleService.deleteRule(id, ruleId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Rule deleted successfully"));
    }

    // =================== Conditions ===================

    @PostMapping("/{id}/conditions")
    public ResponseEntity<ApiResponse<ConditionResponse>> addCondition(
            @PathVariable Long id,
            @Valid @RequestBody CreateConditionRequest request) {
        ConditionResponse response = conditionService.addCondition(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Condition added successfully"));
    }

    @GetMapping("/{id}/conditions")
    public ResponseEntity<ApiResponse<List<ConditionResponse>>> getConditions(@PathVariable Long id) {
        List<ConditionResponse> response = conditionService.getConditions(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}/conditions/{conditionId}")
    public ResponseEntity<ApiResponse<Void>> deleteCondition(
            @PathVariable Long id,
            @PathVariable Long conditionId) {
        conditionService.deleteCondition(id, conditionId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Condition deleted successfully"));
    }

    // =================== Stacking Config ===================

    @PutMapping("/{id}/stacking-config")
    public ResponseEntity<ApiResponse<StackingConfigResponse>> updateStackingConfig(
            @PathVariable Long id,
            @Valid @RequestBody StackingConfigRequest request) {
        StackingConfigResponse response = promotionService.updateStackingConfig(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật cấu hình stacking thành công"));
    }

    // =================== Audit Logs ===================

    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @PathVariable Long id,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<AuditLogResponse> response = auditLogService.getAuditLogs(id, action, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
