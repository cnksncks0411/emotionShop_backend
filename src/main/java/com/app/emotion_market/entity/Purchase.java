package com.app.emotion_market.entity;

import com.app.emotion_market.enums.PurchaseStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_id", nullable = false)
    private SystemEmotion emotion;

    @Column(nullable = false)
    private Integer pointsSpent;

    @Column(columnDefinition = "TEXT")
    private String purchaseMessage;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PurchaseStatus status = PurchaseStatus.ACTIVE;

    @Column(nullable = false)
    private Integer accessCount = 0;

    private LocalDateTime lastAccessedAt;

    private Integer rating; // 1-5점

    @Column(columnDefinition = "TEXT")
    private String reviewComment;

    @Column(nullable = false)
    private LocalDateTime expiresAt; // 구매일 + 7일

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Purchase(User user, SystemEmotion emotion, Integer pointsSpent, 
                   String purchaseMessage) {
        this.user = user;
        this.emotion = emotion;
        this.pointsSpent = pointsSpent;
        this.purchaseMessage = purchaseMessage;
        this.expiresAt = LocalDateTime.now().plusDays(7); // 7일 후 만료
    }

    // 비즈니스 메서드
    public void access() {
        this.accessCount++;
        this.lastAccessedAt = LocalDateTime.now();
    }

    public void addReview(Integer rating, String comment) {
        this.rating = rating;
        this.reviewComment = comment;
    }

    public void expire() {
        this.status = PurchaseStatus.EXPIRED;
    }

    public void refund() {
        this.status = PurchaseStatus.REFUNDED;
    }

    public boolean isActive() {
        return this.status == PurchaseStatus.ACTIVE && !isExpired();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean canAccess() {
        return isActive();
    }

    public long getDaysUntilExpiry() {
        if (isExpired()) return 0;
        return java.time.Duration.between(LocalDateTime.now(), this.expiresAt).toDays();
    }
}
