package com.app.emotion_market.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_emotions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEmotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EmotionType emotionType;

    @Column(nullable = false)
    private Integer intensity; // 1-10

    @Column(nullable = false, columnDefinition = "TEXT")
    private String story;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private LocationType location;

    @Column(columnDefinition = "TEXT[]")
    private String[] tags;

    @Column(nullable = false)
    private Integer pointsEarned = 20;

    @Column(columnDefinition = "jsonb")
    private String bonusDetails; // JSON 형태로 보너스 상세 저장

    @Column(columnDefinition = "jsonb")
    private String reusePermission; // JSON 형태로 재사용 권한 저장

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.APPROVED;

    @Column(columnDefinition = "TEXT")
    private String adminComment;

    private LocalDateTime reviewedAt;

    @Column(length = 255)
    private String reviewedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public UserEmotion(User user, EmotionType emotionType, Integer intensity, 
                      String story, LocationType location, String[] tags,
                      Integer pointsEarned, String bonusDetails, String reusePermission) {
        this.user = user;
        this.emotionType = emotionType;
        this.intensity = intensity;
        this.story = story;
        this.location = location;
        this.tags = tags;
        this.pointsEarned = pointsEarned;
        this.bonusDetails = bonusDetails;
        this.reusePermission = reusePermission;
    }

    // 비즈니스 메서드
    public void approve(String reviewedBy) {
        this.status = ReviewStatus.APPROVED;
        this.reviewedAt = LocalDateTime.now();
        this.reviewedBy = reviewedBy;
    }

    public void reject(String reviewedBy, String reason) {
        this.status = ReviewStatus.REJECTED;
        this.reviewedAt = LocalDateTime.now();
        this.reviewedBy = reviewedBy;
        this.adminComment = reason;
    }

    public boolean isApproved() {
        return this.status == ReviewStatus.APPROVED;
    }

    public boolean isPending() {
        return this.status == ReviewStatus.PENDING;
    }
}
