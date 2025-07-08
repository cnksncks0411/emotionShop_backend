package com.app.emotion_market.service;

import com.app.emotion_market.entity.Purchase;
import com.app.emotion_market.entity.SystemEmotion;
import com.app.emotion_market.entity.User;
import com.app.emotion_market.repository.PurchaseRepository;
import com.app.emotion_market.repository.SystemEmotionRepository;
import com.app.emotion_market.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 감정 구매 처리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final SystemEmotionRepository systemEmotionRepository;
    private final PointService pointService;
    private final SystemEmotionService systemEmotionService;

    /**
     * 감정 구매 처리
     */
    @Transactional
    public Purchase purchaseEmotion(Long userId, Long emotionId, String purchaseMessage) {
        log.info("감정 구매 시작: userId={}, emotionId={}", userId, emotionId);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

        // 2. 감정 상품 조회
        SystemEmotion emotion = systemEmotionRepository.findByIdAndIsActiveTrue(emotionId)
                .orElseThrow(() -> new IllegalArgumentException("구매할 수 없는 감정 상품입니다"));

        // 3. 중복 구매 체크 (7일 내 동일 감정)
        if (isDuplicatePurchase(userId, emotionId)) {
            throw new IllegalStateException("이미 구매한 감정입니다. 7일 후 재구매 가능합니다");
        }

        // 4. 포인트 부족 체크
        if (user.getPoints() < emotion.getPrice()) {
            throw new IllegalStateException("포인트가 부족합니다");
        }

        // 5. 포인트 차감
        pointService.usePoints(userId, emotion.getPrice(), 
                "감정 구매: " + emotion.getName(), emotionId, "EMOTION_PURCHASE");

        // 6. 구매 기록 생성
        Purchase purchase = Purchase.builder()
                .user(user)
                .emotion(emotion)
                .pointsSpent(emotion.getPrice())
                .purchaseMessage(purchaseMessage)
                .status("ACTIVE")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        purchase = purchaseRepository.save(purchase);

        // 7. 감정 상품 구매 수 증가
        systemEmotionService.incrementPurchaseCount(emotionId);

        log.info("감정 구매 완료: purchaseId={}, userId={}, emotionId={}, price={}", 
                purchase.getId(), userId, emotionId, emotion.getPrice());

        return purchase;
    }

    /**
     * 중복 구매 체크 (7일 내 동일 감정)
     */
    private boolean isDuplicatePurchase(Long userId, Long emotionId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return purchaseRepository.existsByUserIdAndEmotionIdAndCreatedAtAfterAndStatus(
                userId, emotionId, sevenDaysAgo, "ACTIVE");
    }

    /**
     * 사용자 구매 내역 조회 (페이징)
     */
    public Page<Purchase> findUserPurchases(Long userId, Pageable pageable) {
        return purchaseRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 사용자 활성 구매 내역 조회 (아직 만료되지 않은 것들)
     */
    public List<Purchase> findUserActivePurchases(Long userId) {
        return purchaseRepository.findByUserIdAndStatusAndExpiresAtAfter(
                userId, "ACTIVE", LocalDateTime.now());
    }

    /**
     * 구매 상세 조회
     */
    public Optional<Purchase> findPurchaseById(Long purchaseId) {
        return purchaseRepository.findById(purchaseId);
    }

    /**
     * 사용자의 특정 구매 조회 (본인 구매만)
     */
    public Optional<Purchase> findUserPurchase(Long userId, Long purchaseId) {
        return purchaseRepository.findByIdAndUserId(purchaseId, userId);
    }

    /**
     * 콘텐츠 접근 가능 여부 체크
     */
    public boolean canAccessContent(Long userId, Long purchaseId) {
        Optional<Purchase> purchaseOpt = findUserPurchase(userId, purchaseId);
        
        if (purchaseOpt.isEmpty()) {
            return false;
        }

        Purchase purchase = purchaseOpt.get();
        return "ACTIVE".equals(purchase.getStatus()) && 
               purchase.getExpiresAt().isAfter(LocalDateTime.now());
    }

    /**
     * 콘텐츠 접근 기록 업데이트
     */
    @Transactional
    public void recordContentAccess(Long userId, Long purchaseId) {
        Purchase purchase = findUserPurchase(userId, purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구매 내역입니다"));

        if (!canAccessContent(userId, purchaseId)) {
            throw new IllegalStateException("이용 기간이 만료되었습니다");
        }

        purchase.setAccessCount(purchase.getAccessCount() + 1);
        purchase.setLastAccessedAt(LocalDateTime.now());

        log.info("콘텐츠 접근 기록: purchaseId={}, accessCount={}", 
                purchaseId, purchase.getAccessCount());
    }

    /**
     * 구매 리뷰 작성
     */
    @Transactional
    public void writeReview(Long userId, Long purchaseId, Integer rating, String reviewComment) {
        Purchase purchase = findUserPurchase(userId, purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구매 내역입니다"));

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1-5점 사이여야 합니다");
        }

        purchase.setRating(rating);
        purchase.setReviewComment(reviewComment);

        // 감정 상품의 평균 평점 재계산 (별도 메서드에서 처리)
        updateEmotionAverageRating(purchase.getEmotion().getId());

        log.info("구매 리뷰 작성: purchaseId={}, rating={}", purchaseId, rating);
    }

    /**
     * 감정 상품 평균 평점 재계산
     */
    @Transactional
    public void updateEmotionAverageRating(Long emotionId) {
        Double averageRating = purchaseRepository.getAverageRatingByEmotionId(emotionId);
        if (averageRating != null) {
            systemEmotionService.updateAverageRating(emotionId, averageRating);
        }
    }

    /**
     * 만료된 구매 상태 업데이트 (스케줄러에서 호출)
     */
    @Transactional
    public void updateExpiredPurchases() {
        int updatedCount = purchaseRepository.updateExpiredPurchases(LocalDateTime.now());
        if (updatedCount > 0) {
            log.info("만료된 구매 상태 업데이트: {}건", updatedCount);
        }
    }

    /**
     * 곧 만료될 구매 조회 (알림용)
     */
    public List<Purchase> findPurchasesExpiringWithin(int hours) {
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(hours);
        return purchaseRepository.findByStatusAndExpiresAtBetween(
                "ACTIVE", LocalDateTime.now(), expiryTime);
    }

    /**
     * 사용자 구매 통계
     */
    public PurchaseStats getUserPurchaseStats(Long userId) {
        Long totalPurchases = purchaseRepository.countByUserId(userId);
        Long activePurchases = purchaseRepository.countByUserIdAndStatusAndExpiresAtAfter(
                userId, "ACTIVE", LocalDateTime.now());
        Integer totalSpent = purchaseRepository.getTotalPointsSpentByUserId(userId);

        return PurchaseStats.builder()
                .totalPurchases(totalPurchases)
                .activePurchases(activePurchases)
                .totalPointsSpent(totalSpent != null ? totalSpent : 0)
                .build();
    }

    /**
     * 구매 통계 DTO
     */
    public static class PurchaseStats {
        private final Long totalPurchases;
        private final Long activePurchases;
        private final Integer totalPointsSpent;

        public PurchaseStats(Long totalPurchases, Long activePurchases, Integer totalPointsSpent) {
            this.totalPurchases = totalPurchases;
            this.activePurchases = activePurchases;
            this.totalPointsSpent = totalPointsSpent;
        }

        public static PurchaseStatsBuilder builder() {
            return new PurchaseStatsBuilder();
        }

        public Long getTotalPurchases() { return totalPurchases; }
        public Long getActivePurchases() { return activePurchases; }
        public Integer getTotalPointsSpent() { return totalPointsSpent; }

        public static class PurchaseStatsBuilder {
            private Long totalPurchases;
            private Long activePurchases;
            private Integer totalPointsSpent;

            public PurchaseStatsBuilder totalPurchases(Long totalPurchases) {
                this.totalPurchases = totalPurchases;
                return this;
            }

            public PurchaseStatsBuilder activePurchases(Long activePurchases) {
                this.activePurchases = activePurchases;
                return this;
            }

            public PurchaseStatsBuilder totalPointsSpent(Integer totalPointsSpent) {
                this.totalPointsSpent = totalPointsSpent;
                return this;
            }

            public PurchaseStats build() {
                return new PurchaseStats(totalPurchases, activePurchases, totalPointsSpent);
            }
        }
    }
}
