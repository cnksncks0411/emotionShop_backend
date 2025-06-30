package com.app.emotion_market.service;

import com.app.emotion_market.entity.*;
import com.app.emotion_market.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PointTransactionService {

    private final PointTransactionRepository pointTransactionRepository;

    @Transactional
    public PointTransaction recordSignupBonus(User user) {
        PointTransaction transaction = PointTransaction.createEarnTransaction(
                user,
                100, // 신규 가입 보너스
                "신규 가입 축하 보너스",
                null,
                RelatedType.SIGNUP_BONUS,
                user.getPoints()
        );

        PointTransaction saved = pointTransactionRepository.save(transaction);
        log.info("가입 보너스 지급: userId={}, points={}", user.getId(), 100);
        return saved;
    }

    @Transactional
    public PointTransaction recordEmotionSale(User user, UserEmotion emotion) {
        PointTransaction transaction = PointTransaction.createEarnTransaction(
                user,
                emotion.getPointsEarned(),
                String.format("감정 판매 (%s)", emotion.getEmotionType().getKoreanName()),
                emotion.getId(),
                RelatedType.EMOTION_SALE,
                user.getPoints()
        );

        PointTransaction saved = pointTransactionRepository.save(transaction);
        log.info("감정 판매 포인트 지급: userId={}, emotionId={}, points={}", 
                user.getId(), emotion.getId(), emotion.getPointsEarned());
        return saved;
    }

    @Transactional
    public PointTransaction recordEmotionPurchase(User user, Purchase purchase) {
        PointTransaction transaction = PointTransaction.createSpendTransaction(
                user,
                purchase.getPointsSpent(),
                String.format("감정 구매 (%s)", purchase.getEmotion().getName()),
                purchase.getId(),
                RelatedType.EMOTION_PURCHASE,
                user.getPoints()
        );

        PointTransaction saved = pointTransactionRepository.save(transaction);
        log.info("감정 구매 포인트 차감: userId={}, purchaseId={}, points={}", 
                user.getId(), purchase.getId(), purchase.getPointsSpent());
        return saved;
    }

    @Transactional
    public PointTransaction recordEmotionRefund(User user, UserEmotion emotion) {
        PointTransaction transaction = PointTransaction.createSpendTransaction(
                user,
                emotion.getPointsEarned(),
                String.format("감정 거부로 인한 포인트 회수 (%s)", emotion.getEmotionType().getKoreanName()),
                emotion.getId(),
                RelatedType.REFUND,
                user.getPoints()
        );

        PointTransaction saved = pointTransactionRepository.save(transaction);
        log.info("감정 거부 포인트 회수: userId={}, emotionId={}, points={}", 
                user.getId(), emotion.getId(), emotion.getPointsEarned());
        return saved;
    }

    @Transactional
    public PointTransaction recordAdminAdjustment(User user, Integer amount, String reason) {
        TransactionType type = amount > 0 ? TransactionType.EARNED : TransactionType.SPENT;
        String description = String.format("관리자 조정: %s", reason);

        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .amount(amount)
                .transactionType(type)
                .description(description)
                .relatedType(RelatedType.ADMIN_ADJUSTMENT)
                .balanceAfter(user.getPoints())
                .build();

        PointTransaction saved = pointTransactionRepository.save(transaction);
        log.info("관리자 포인트 조정: userId={}, amount={}, reason={}", 
                user.getId(), amount, reason);
        return saved;
    }

    public List<PointTransaction> getUserTransactionHistory(User user) {
        return pointTransactionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Page<PointTransaction> getUserTransactionsWithFilters(Long userId, TransactionType transactionType,
                                                                LocalDateTime startDate, LocalDateTime endDate,
                                                                Pageable pageable) {
        return pointTransactionRepository.findTransactionsWithFilters(userId, transactionType, startDate, endDate, pageable);
    }

    public Integer getTotalEarnedByUser(Long userId) {
        return pointTransactionRepository.getTotalEarnedByUser(userId);
    }

    public Integer getTotalSpentByUser(Long userId) {
        return pointTransactionRepository.getTotalSpentByUser(userId);
    }

    public List<PointTransaction> getRecentTransactions(User user, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return pointTransactionRepository.findRecentTransactionsByUser(user, startDate);
    }

    public List<Object[]> getMonthlyTransactionStats(Long userId, int months) {
        return pointTransactionRepository.getMonthlyTransactionStats(userId, months);
    }

    public List<Object[]> getDailyTransactionStats(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return pointTransactionRepository.getDailyTransactionStats(userId, startDate, endDate);
    }

    public Integer getUserEarningsInPeriod(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return pointTransactionRepository.sumAmountByUserInPeriod(user, startDate, endDate);
    }

    // 통계 메서드들
    public Long countTransactionsByTypeToday(TransactionType type) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return pointTransactionRepository.countByTypeAfterDate(type, startOfDay);
    }

    public Integer sumAmountByTypeInPeriod(TransactionType type, LocalDateTime startDate, LocalDateTime endDate) {
        return pointTransactionRepository.sumAmountByTypeInPeriod(type, startDate, endDate);
    }
}
