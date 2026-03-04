package org.example.promotion.service;

import org.example.promotion.dto.request.CreatePromotionRequest;
import org.example.promotion.dto.response.PromotionResponse;
import org.example.promotion.entity.Promotion;
import org.example.promotion.enums.PromotionStatus;
import org.example.promotion.exception.InvalidStatusException;
import org.example.promotion.exception.ResourceNotFoundException;
import org.example.promotion.mapper.PromotionMapper;
import org.example.promotion.repository.PromotionRepository;
import org.example.promotion.repository.PromotionStackingRuleRepository;
import org.example.promotion.service.impl.PromotionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private PromotionStackingRuleRepository stackingRuleRepository;

    @Mock
    private IAuditLogService auditLogService;

    @Mock
    private PromotionMapper mapper;

    @InjectMocks
    private PromotionServiceImpl promotionService;

    private Promotion promotion;
    private CreatePromotionRequest createRequest;
    private PromotionResponse promotionResponse;

    @BeforeEach
    void setUp() {
        promotion = new Promotion();
        promotion.setId(1L);
        promotion.setName("Summer Sale");
        promotion.setStatus(PromotionStatus.DRAFT);
        promotion.setIsDeleted(false);

        createRequest = new CreatePromotionRequest();
        createRequest.setName("Summer Sale");

        promotionResponse = new PromotionResponse();
        promotionResponse.setId(1L);
        promotionResponse.setName("Summer Sale");
        promotionResponse.setStatus(PromotionStatus.DRAFT);
    }

    @Test
    void createPromotion_Success() {
        when(mapper.toEntity(createRequest)).thenReturn(promotion);
        when(promotionRepository.save(any(Promotion.class))).thenReturn(promotion);
        when(mapper.toResponse(promotion)).thenReturn(promotionResponse);

        PromotionResponse result = promotionService.createPromotion(createRequest);

        assertNotNull(result);
        assertEquals("Summer Sale", result.getName());
        assertEquals(PromotionStatus.DRAFT, result.getStatus());
        verify(promotionRepository).save(any(Promotion.class));
        verify(auditLogService).saveAuditLog(eq(1L), eq("CREATE"), isNull());
    }

    @Test
    void getPromotionById_Success() {
        when(promotionRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(promotion));
        when(mapper.toDetailResponse(promotion)).thenReturn(promotionResponse);

        PromotionResponse result = promotionService.getPromotionById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(promotionRepository).findByIdAndIsDeletedFalse(1L);
    }

    @Test
    void getPromotionById_NotFound_ThrowsException() {
        when(promotionRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> promotionService.getPromotionById(1L));
        verify(promotionRepository).findByIdAndIsDeletedFalse(1L);
    }

    @Test
    void deletePromotion_Success() {
        when(promotionRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(promotion));

        promotionService.deletePromotion(1L);

        assertTrue(promotion.getIsDeleted());
        verify(promotionRepository).save(promotion);
        verify(auditLogService).saveAuditLog(eq(1L), eq("DELETE"), isNull());
    }

    @Test
    void deletePromotion_NotDraft_ThrowsException() {
        promotion.setStatus(PromotionStatus.ACTIVE);
        when(promotionRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(promotion));

        assertThrows(InvalidStatusException.class, () -> promotionService.deletePromotion(1L));
        verify(promotionRepository, never()).save(any());
        verify(auditLogService, never()).saveAuditLog(any(), any(), any());
    }
}
