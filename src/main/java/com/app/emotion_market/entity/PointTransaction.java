package com.app.emotion_market.entity;

import com.app.emotion_market.enums.RelatedType;
import com.app.emotion_market.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer amount; // 양수: 획득, 음수: 사용

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private Long relatedId; // 관련 레코드 ID (감정 판매/구매 ID)

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private RelatedType relatedType;

    @Column(nullable = false)
    private Integer balanceAfter; // 거래 후 잔액

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PointTransaction(User user, Integer amount, TransactionType transactionType,
                           String description, Long relatedId, RelatedType relatedType,
                           Integer balanceAfter) {
        this.user = user;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        this.relatedId = relatedId;
        this.relatedType = relatedType;
        this.balanceAfter = balanceAfter;
    }

    // 정적 팩토리 메서드
    public static PointTransaction createEarnTransaction(User user, Integer amount, 
                                                        String description, Long relatedId, 
                                                        RelatedType relatedType, Integer balanceAfter) {
        return PointTransaction.builder()
                .user(user)
                .amount(Math.abs(amount)) // 양수로 저장
                .transactionType(TransactionType.EARNED)
                .description(description)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .balanceAfter(balanceAfter)
                .build();
    }

    public static PointTransaction createSpendTransaction(User user, Integer amount, 
                                                         String description, Long relatedId, 
                                                         RelatedType relatedType, Integer balanceAfter) {
        return PointTransaction.builder()
                .user(user)
                .amount(-Math.abs(amount)) // 음수로 저장
                .transactionType(TransactionType.SPENT)
                .description(description)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .balanceAfter(balanceAfter)
                .build();
    }

    // 비즈니스 메서드
    public boolean isEarned() {
        return this.amount > 0;
    }

    public boolean isSpent() {
        return this.amount < 0;
    }

    public Integer getAbsoluteAmount() {
        return Math.abs(this.amount);
    }
}
