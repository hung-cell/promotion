package org.example.promotion.management.service;

import org.example.promotion.management.dto.response.AuditLogResponse;
import org.example.promotion.common.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Map;

public interface IAuditLogService {
    void saveAuditLog(Long promotionId, String action, Map<String, Object> changes);

    PageResponse<AuditLogResponse> getAuditLogs(Long promotionId, String action, LocalDateTime fromDate,
            LocalDateTime toDate, Pageable pageable);
}
