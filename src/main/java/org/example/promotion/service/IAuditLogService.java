package org.example.promotion.service;

import org.example.promotion.dto.response.AuditLogResponse;
import org.example.promotion.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Map;

public interface IAuditLogService {
    void saveAuditLog(Long promotionId, String action, Map<String, Object> changes);

    PageResponse<AuditLogResponse> getAuditLogs(Long promotionId, String action, LocalDateTime fromDate,
            LocalDateTime toDate, Pageable pageable);
}
