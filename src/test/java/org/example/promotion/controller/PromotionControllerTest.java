package org.example.promotion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.promotion.dto.request.CreatePromotionRequest;
import org.example.promotion.dto.response.PromotionResponse;
import org.example.promotion.enums.PromotionStatus;
import org.example.promotion.enums.PromotionType;
import org.example.promotion.service.IAuditLogService;
import org.example.promotion.service.IPromotionConditionService;
import org.example.promotion.service.IPromotionRuleService;
import org.example.promotion.service.IPromotionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(PromotionController.class)
@WithMockUser
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IPromotionService promotionService;

    @MockBean
    private IPromotionRuleService ruleService;

    @MockBean
    private IPromotionConditionService conditionService;

    @MockBean
    private IAuditLogService auditLogService;

    private PromotionResponse promotionResponse;

    @BeforeEach
    void setUp() {
        promotionResponse = PromotionResponse.builder()
                .id(1L)
                .name("Summer Sale")
                .description("10% off for Summer")
                .type(PromotionType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .status(PromotionStatus.DRAFT)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    void createPromotion_Success() throws Exception {
        CreatePromotionRequest request = new CreatePromotionRequest();
        request.setName("Summer Sale");
        request.setDescription("10% off for Summer");
        request.setType(PromotionType.PERCENTAGE);
        request.setDiscountValue(new BigDecimal("10"));
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(30));

        when(promotionService.createPromotion(any(CreatePromotionRequest.class)))
                .thenReturn(promotionResponse);

        mockMvc.perform(post("/api/v1/promotions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Promotion created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Summer Sale"));
    }

    @Test
    void getPromotionById_Success() throws Exception {
        when(promotionService.getPromotionById(1L)).thenReturn(promotionResponse);

        mockMvc.perform(get("/api/v1/promotions/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Summer Sale"));
    }

    @Test
    void activatePromotion_Success() throws Exception {
        promotionResponse.setStatus(PromotionStatus.ACTIVE);
        when(promotionService.activatePromotion(1L)).thenReturn(promotionResponse);

        mockMvc.perform(post("/api/v1/promotions/1/activate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Promotion activated successfully"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void deletePromotion_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/promotions/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Promotion deleted successfully"));
    }
}
