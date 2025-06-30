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
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private Integer points = 100; // 신규 가입 보너스

    @Column(length = 4)
    private String mbtiType;

    @Column(length = 500)
    private String profileImage;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(nullable = false)
    private Boolean agreeTerms = false;

    @Column(nullable = false)
    private Boolean agreePrivacy = false;

    @Column(nullable = false)
    private Boolean agreeMarketing = false;

    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public User(String email, String password, String nickname, String mbtiType, 
                String profileImage, Boolean agreeTerms, Boolean agreePrivacy, 
                Boolean agreeMarketing) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.mbtiType = mbtiType;
        this.profileImage = profileImage;
        this.agreeTerms = agreeTerms;
        this.agreePrivacy = agreePrivacy;
        this.agreeMarketing = agreeMarketing;
    }

    // 비즈니스 메서드
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void addPoints(Integer points) {
        this.points += points;
    }

    public void subtractPoints(Integer points) {
        if (this.points < points) {
            throw new IllegalArgumentException("포인트가 부족합니다");
        }
        this.points -= points;
    }

    public void updateProfile(String nickname, String profileImage, String mbtiType) {
        if (nickname != null) this.nickname = nickname;
        if (profileImage != null) this.profileImage = profileImage;
        if (mbtiType != null) this.mbtiType = mbtiType;
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
}
