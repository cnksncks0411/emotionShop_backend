package com.app.emotion_market.service;

import com.emotionshop.entity_market.entity.PointTransaction;
import com.emotionshop.entity_market.entity.User;
import com.emotionshop.entity_market.repository.PointTransactionRepository;
import com.emotionshop.entity_market.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 포인트 거래 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PointService {

    private final UserRepository userRepository;
    private final PointTransactionRepository pointTransactionRepository;

    /**
     * 포인트 적립
     */
    @Transactional
    public PointTransaction earnPoints(Long userId, Integer amount, String description, 
                                     Long relatedId, String relatedType) {
        log.info("포인트 적립 시작: userId={}, amount={}", userId, amount);

        if (amount <= 0) {
            throw new IllegalArgumentException("적립 포인트는 0보다 커야 합니다");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

        // 포인트 적립
        user.setPoints(user.getPoints() + amount);
        user = userRepository.save(user);

        // 거래 내역 기록
        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .amount(amount)
                .transactionType("EARNED")
                .description(description)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .balanceAfter(user.getPoints())
                .build();

        transaction = pointTransactionRepository.save(transaction);

        log.info("포인트 적립 완료: userId={}, amount={}, newBalance={}", 
                userId, amount, user.getPoints());

        return transaction;
    }

    /**
     * 포인트 사용
     */
    @Transactional
    public PointTransaction usePoints(Long userId, Integer amount, String description,
                                    Long relatedId, String relatedType) {
        log.info("포인트 사용 시작: userId={}, amount={}", userId, amount);

        if (amount <= 0) {
            throw new IllegalArgumentException("사용 포인트는 0보다 커야 합니다");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

        // 포인트 부족 체크
        if (user.getPoints() < amount) {
            throw new IllegalStateException("포인트가 부족합니다. 현재: " + user.getPoints() + "EP, 필요: " + amount + "EP");
        }

        // 포인트 차감
        user.setPoints(user.getPoints() - amount);
        user = userRepository.save(user);

        // 거래 내역 기록
        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .amount(-amount) // 사용은 음수로 기록
                .transactionType("SPENT")
                .description(description)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .balanceAfter(user.getPoints())
                .build();

        transaction = pointTransactionRepository.save(transaction);

        log.info("포인트 사용 완료: userId={}, amount={}, newBalance={}", 
                userId, amount, user.getPoints());

        return transaction;
    }

    /**
     * 신규 가입 보너스 지급
     */
    @Transactional
    public PointTransaction giveSignupBonus(Long userId) {
        return earnPoints(userId, 100, "신규 가입 보너스", userId, "SIGNUP_BONUS");
    }

    /**
     * 감정 판매 포인트 계산 및 지급
     */
    @Transactional
    public PointTransaction giveEmotionSalePoints(Long userId, Long emotionSaleId, 
                                                String story, boolean reusePermission) {
        int basePoints = 20; // 기본 포인트
        int bonusPoints = 0;
        StringBuilder description = new StringBuilder("감정 판매");

        // 상세 스토리 보너스 (100자 이상)
        if (story != null && story.length() >= 100) {
            bonusPoints += 5;
            description.append(" + 상세스토리 보너스(+5EP)");
        }

        // 재사용 허용 보너스
        if (reusePermission) {
            bonusPoints += 5;
            description.append(" + 재사용허용 보너스(+5EP)");
        }

        int totalPoints = basePoints + bonusPoints;
        
        return earnPoints(userId, totalPoints, description.toString(), emotionSaleId, "EMOTION_SALE");
    }

    /**
     * 사용자 포인트 잔액 조회
     */
    public Integer getUserPoints(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));
        return user.getPoints();
    }

    /**
     * 사용자 포인트 거래 내역 조회 (페이징)
     */
    public Page<PointTransaction> getUserTransactions(Long userId, Pageable pageable) {
        return pointTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 사용자 포인트 거래 내역 조회 (거래 유형별)
     */
    public Page<PointTransaction> getUserTransactionsByType(Long userId, String transactionType, Pageable pageable) {
        return pointTransactionRepository.findByUserIdAndTransactionTypeOrderByCreatedAtDesc(userId, transactionType, pageable);
    }

    /**
     * 사용자 포인트 통계 조회
     */
    public PointStats getUserPointStats(Long userId) {
        Integer totalEarned = pointTransactionRepository.getTotalEarnedByUserId(userId);
        Integer totalSpent = pointTransactionRepository.getTotalSpentByUserId(userId);
        Integer currentBalance = getUserPoints(userId);
        Long transactionCount = pointTransactionRepository.countByUserId(userId);

        return PointStats.builder()
                .currentBalance(currentBalance)
                .totalEarned(totalEarned != null ? totalEarned : 0)
                .totalSpent(Math.abs(totalSpent != null ? totalSpent : 0)) // 절댓값으로 변환
                .transactionCount(transactionCount)
                .build();
    }

    /**
     * 포인트 통계 DTO
     */
    public static class PointStats {
        private final Integer currentBalance;
        private final Integer totalEarned;
        private final Integer totalSpent;
        private final Long transactionCount;

        public PointStats(Integer currentBalance, Integer totalEarned, Integer totalSpent, Long transactionCount) {
            this.currentBalance = currentBalance;
            this.totalEarned = totalEarned;
            this.totalSpent = totalSpent;
            this.transactionCount = transactionCount;
        }

        public static PointStatsBuilder builder() {
            return new PointStatsBuilder();
        }

        public Integer getCurrentBalance() { return currentBalance; }
        public Integer getTotalEarned() { return totalEarned; }
        public Integer getTotalSpent() { return totalSpent; }
        public Long getTransactionCount() { return transactionCount; }

        public static class PointStatsBuilder {
            private Integer currentBalance;
            private Integer totalEarned;
            private Integer totalSpent;
            private Long transactionCount;

            public PointStatsBuilder currentBalance(Integer currentBalance) {
                this.currentBalance = currentBalance;
                return this;
            }

            public PointStatsBuilder totalEarned(Integer totalEarned) {
                this.totalEarned = totalEarned;
                return this;
            }

            public PointStatsBuilder totalSpent(Integer totalSpent) {
                this.totalSpent = totalSpent;
                return this;
            }

            public PointStatsBuilder transactionCount(Long transactionCount) {
                this.transactionCount = transactionCount;
                return this;
            }

            public PointStats build() {
                return new PointStats(currentBalance, totalEarned, totalSpent, transactionCount);
            }
        }
    }
}
