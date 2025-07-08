package com.app.emotion_market.service;

import com.app.emotion_market.entity.PointTransaction;
import com.app.emotion_market.entity.User;
import com.app.emotion_market.repository.PointTransactionRepository;
import com.app.emotion_market.repository.UserRepository;
import com.app.emotion_market.enums.RelatedType;
import com.app.emotion_market.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 포인트 거래 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final UserRepository userRepository;
    private final PointTransactionRepository pointTransactionRepository;

    /**
     * 포인트 추가
     */
    public void addPoints(Long userId, Integer amount, String transactionType, 
                         String description, Long relatedId, String relatedType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 사용자 포인트 증가
        user.addPoints(amount);
        
        // 포인트 거래 내역 생성
        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .amount(amount)
                .transactionType(TransactionType.EARNED)
                .description(description)
                .relatedId(relatedId)
                .relatedType(RelatedType.valueOf(relatedType))
                .balanceAfter(user.getPoints())
                .build();

        pointTransactionRepository.save(transaction);
        log.info("포인트 추가 완료 - User: {}, Amount: {}, Balance: {}", userId, amount, user.getPoints());
    }

    /**
     * 포인트 차감
     */
    public void subtractPoints(Long userId, Integer amount, String transactionType,
                              String description, Long relatedId, String relatedType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 포인트 부족 체크
        if (user.getPoints() < amount) {
            throw new RuntimeException("포인트가 부족합니다.");
        }

        // 사용자 포인트 차감
        user.subtractPoints(amount);

        // 포인트 거래 내역 생성
        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .amount(-amount) // 차감은 음수로 저장
                .transactionType(TransactionType.SPENT)
                .description(description)
                .relatedId(relatedId)
                .relatedType(RelatedType.valueOf(relatedType))
                .balanceAfter(user.getPoints())
                .build();

        pointTransactionRepository.save(transaction);
        log.info("포인트 차감 완료 - User: {}, Amount: {}, Balance: {}", userId, amount, user.getPoints());
    }
}
