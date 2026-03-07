package org.example.promotion.management.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogResponse {

    private Long id;
    private Long promotionId;
    private String action;
    private String userId;
    private String userName;
    private LocalDateTime timestamp;
    private Map<String, Object> changes;
}
