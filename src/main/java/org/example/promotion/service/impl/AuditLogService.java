package org.example.promotion.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.promotion.dto.response.AuditLogResponse;
import org.example.promotion.dto.response.PageResponse;
import org.example.promotion.entity.PromotionAuditLog;
import org.example.promotion.mapper.PromotionMapper;
import org.example.promotion.repository.PromotionAuditLogRepository;
import org.example.promotion.service.IAuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService implements IAuditLogService {

    private final PromotionAuditLogRepository auditLogRepository;
    private final PromotionMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAuditLog(Long promotionId, String action, Map<String, Object> changes) {
        String changesJson = null;
        if (changes != null && !changes.isEmpty()) {
            try {
                changesJson = objectMapper.writeValueAsString(changes);
            } catch (Exception e) {
                log.error("Failed to serialize audit log changes for promotion {}", promotionId, e);
            }
        }

        PromotionAuditLog logEntry = PromotionAuditLog.builder()
                .promotionId(promotionId)
                .action(action)
                .userId("system") // TODO: get from SecurityContext
                .userName("System User") // TODO: get from SecurityContext
                .changes(changesJson)
                .build();

        auditLogRepository.save(logEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogs(Long promotionId, String action, LocalDateTime fromDate,
            LocalDateTime toDate, Pageable pageable) {
        Page<PromotionAuditLog> page = auditLogRepository.findByPromotionIdWithFilters(promotionId, action, fromDate,
                toDate, pageable);

        List<AuditLogResponse> content = page.getContent().stream()
                .map(mapper::toAuditLogResponse)
                .collect(Collectors.toList());

        return mapper.toPageResponse(page, content);
    }
}
