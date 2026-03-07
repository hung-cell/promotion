package org.example.promotion.engine.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.promotion.common.enums.ReservationStatus;
import org.example.promotion.engine.cache.PromotionCacheService;
import org.example.promotion.engine.dto.request.CommitRequest;
import org.example.promotion.engine.dto.request.ReleaseRequest;
import org.example.promotion.engine.dto.request.ReserveRequest;
import org.example.promotion.engine.dto.response.ReservationResponse;
import org.example.promotion.engine.dto.response.ReservationResponse.ReservedPromotion;
import org.example.promotion.engine.entity.PromotionReservation;
import org.example.promotion.engine.entity.PromotionUsage;
import org.example.promotion.engine.repository.PromotionReservationRepository;
import org.example.promotion.engine.repository.PromotionUsageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionReservationService {

    private final PromotionCacheService cacheService;
    private final PromotionReservationRepository reservationRepository;
    private final PromotionUsageRepository usageRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ReservationResponse reserve(ReserveRequest request) {
        log.info("[Reserve] orderId={}, promotionIds={}", request.getOrderId(), request.getPromotionIds());

        String reservationId = "rsv_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(request.getTtlSeconds());

        List<ReservedPromotion> reserved = new ArrayList<>();
        List<Long> successIds = new ArrayList<>();

        for (Long promotionId : request.getPromotionIds()) {
            boolean success = cacheService.tryReserveQuota(promotionId);
            if (success) {
                successIds.add(promotionId);
                reserved.add(ReservedPromotion.builder()
                        .promotionId(promotionId)
                        .reserved(true)
                        .reservedAt(LocalDateTime.now())
                        .build());
            } else {
                // Roll back all previously reserved quotas in this request
                successIds.forEach(cacheService::releaseQuota);
                reserved.clear();
                log.warn("[Reserve] Quota exceeded for promotionId={}, rolling back", promotionId);
                throw new RuntimeException("Chương trình khuyến mãi id=" + promotionId + " đã hết lượt sử dụng");
            }
        }

        // Persist reservation to MySQL engine DB
        PromotionReservation entity = PromotionReservation.builder()
                .reservationId(reservationId)
                .orderId(request.getOrderId())
                .customerId(request.getCustomerId())
                .promotionIds(toJson(request.getPromotionIds()))
                .couponCodes(toJson(request.getCouponCodes()))
                .status(ReservationStatus.PENDING)
                .expiresAt(expiresAt)
                .build();
        reservationRepository.save(entity);

        return ReservationResponse.builder()
                .reservationId(reservationId)
                .orderId(request.getOrderId())
                .reservedPromotions(reserved)
                .expiresAt(expiresAt)
                .ttlSeconds(request.getTtlSeconds())
                .build();
    }

    @Transactional
    public void commit(CommitRequest request) {
        log.info("[Commit] reservationId={}, orderId={}", request.getReservationId(), request.getOrderId());

        PromotionReservation reservation = reservationRepository
                .findByReservationId(request.getReservationId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy reservation: " + request.getReservationId()));

        if (!ReservationStatus.PENDING.equals(reservation.getStatus())) {
            throw new RuntimeException("Reservation không ở trạng thái PENDING: " + request.getReservationId());
        }

        // Persist usage records to MySQL
        List<Long> promotionIds = fromJson(reservation.getPromotionIds());
        for (Long promotionId : promotionIds) {
            PromotionUsage usage = PromotionUsage.builder()
                    .promotionId(promotionId)
                    .customerId(request.getCustomerId())
                    .orderId(request.getOrderId())
                    .discountAmount(request.getDiscountAmount())
                    .reservationId(request.getReservationId())
                    .build();
            usageRepository.save(usage);
        }

        // Mark reservation committed
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setCommittedAt(LocalDateTime.now());
        reservationRepository.save(reservation);
    }

    @Transactional
    public void release(ReleaseRequest request) {
        log.info("[Release] reservationId={}, reason={}", request.getReservationId(), request.getReason());

        PromotionReservation reservation = reservationRepository
                .findByReservationId(request.getReservationId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy reservation: " + request.getReservationId()));

        if (!ReservationStatus.PENDING.equals(reservation.getStatus())) {
            throw new RuntimeException("Reservation không ở trạng thái PENDING: " + request.getReservationId());
        }

        // Return quota to Redis
        List<Long> promotionIds = fromJson(reservation.getPromotionIds());
        promotionIds.forEach(cacheService::releaseQuota);

        // Mark released in DB
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setReleasedAt(LocalDateTime.now());
        reservation.setReleaseReason(request.getReason());
        reservationRepository.save(reservation);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    @SuppressWarnings("unchecked")
    private List<Long> fromJson(String json) {
        try {
            List<?> raw = objectMapper.readValue(json, List.class);
            return raw.stream().map(o -> Long.parseLong(o.toString())).toList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
