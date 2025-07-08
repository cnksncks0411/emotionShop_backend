package com.app.emotion_market.entity;

import com.app.emotion_market.enumType.EmotionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_emotions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SystemEmotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EmotionType emotionType;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 10)
    private String emoji;

    @Column(nullable = false)
    private Integer categoryId; // 1-8 카테고리

    @Column(nullable = false)
    private Integer price;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer totalPurchases = 0;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String contents; // JSON 형태로 콘텐츠 저장

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public SystemEmotion(EmotionType emotionType, String name, String description,
                        String emoji, Integer categoryId, Integer price,
                        String contents, Boolean isActive, Integer sortOrder) {
        this.emotionType = emotionType;
        this.name = name;
        this.description = description;
        this.emoji = emoji;
        this.categoryId = categoryId;
        this.price = price;
        this.contents = contents;
        this.isActive = isActive != null ? isActive : true;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    // 비즈니스 메서드
    public void increasePurchaseCount() {
        this.totalPurchases++;
    }

    public void updateAverageRating(BigDecimal newRating) {
        this.averageRating = newRating;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updateContents(String contents) {
        this.contents = contents;
    }

    public void updatePrice(Integer price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return this.isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public double getAverageRating() {
        return this.averageRating != null ? this.averageRating.doubleValue() : 0.0;
    }
}
