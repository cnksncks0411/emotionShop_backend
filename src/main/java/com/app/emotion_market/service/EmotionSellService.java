package com.app.emotion_market.service;

import com.app.emotion_market.entity.*;
import com.app.emotion_market.enumType.EmotionType;
import com.app.emotion_market.enumType.LocationType;
import com.app.emotion_market.enumType.ReviewStatus;
import com.app.emotion_market.repository.UserEmotionRepository;
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
public class EmotionSellService {

    private final UserEmotionRepository userEmotionRepository;
    private final UserService userService;
    private final PointTransactionService pointTransactionService;

    private static final int DAILY_SALE_LIMIT = 5;
    private static final int BASE_POINTS = 20;
    private static final int DETAILED_STORY_BONUS = 5;
    private static final int REUSE_PERMISSION_BONUS = 5;
    private static final int DETAILED_STORY_MIN_LENGTH = 100;

    @Transactional
    public UserEmotion sellEmotion(Long userId, EmotionType emotionType, Integer intensity,
                                   String story, LocationType location, String[] tags,
                                   boolean allowResale, boolean allowCreativeUse) {
        
        // 사용자 조회
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 일일 판매 제한 체크
        validateDailySaleLimit(user);

        // 입력 데이터 유효성 검증
        validateEmotionData(emotionType, intensity, story);

        // 포인트 계산
        int earnedPoints = calculatePoints(story, allowResale, allowCreativeUse);

        // 재사용 권한 JSON 생성
        String reusePermission = createReusePermissionJson(allowResale, allowCreativeUse);

        // 보너스 상세 JSON 생성
        String bonusDetails = createBonusDetailsJson(story, allowResale, allowCreativeUse);

        // 감정 엔티티 생성
        UserEmotion emotion = UserEmotion.builder()
                .user(user)
                .emotionType(emotionType)
                .intensity(intensity)
                .story(story)
                .location(location)
                .tags(tags)
                .pointsEarned(earnedPoints)
                .bonusDetails(bonusDetails)
                .reusePermission(reusePermission)
                .build();

        UserEmotion savedEmotion = userEmotionRepository.save(emotion);

        // 포인트 지급 및 트랜잭션 기록
        userService.addPoints(userId, earnedPoints);
        pointTransactionService.recordEmotionSale(user, savedEmotion);

        log.info("감정 판매 완료: userId={}, emotionType={}, points={}", 
                userId, emotionType, earnedPoints);

        return savedEmotion;
    }

    public boolean canSellToday(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        
        Long todaySales = userEmotionRepository.countTodaySalesByUser(user, ReviewStatus.APPROVED);
        return todaySales < DAILY_SALE_LIMIT;
    }

    public int getRemainingDailySales(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        
        Long todaySales = userEmotionRepository.countTodaySalesByUser(user, ReviewStatus.APPROVED);
        return Math.max(0, DAILY_SALE_LIMIT - todaySales.intValue());
    }

    public List<UserEmotion> getUserEmotionHistory(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        
        return userEmotionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Page<UserEmotion> getUserEmotionsWithFilters(Long userId, EmotionType emotionType,
                                                        LocalDateTime startDate, LocalDateTime endDate,
                                                        Pageable pageable) {
        return userEmotionRepository.findUserEmotionsWithFilters(userId, emotionType, startDate, endDate, pageable);
    }

    public List<UserEmotion> getTopEmotionsByUser(Long userId, int limit) {
        return userEmotionRepository.findTopEmotionsByUser(userId, limit);
    }

    public List<Object[]> getEmotionStatsByUser(Long userId) {
        return userEmotionRepository.getEmotionStatsByUser(userId);
    }

    // 비즈니스 로직 메서드들
    private void validateDailySaleLimit(User user) {
        Long todaySales = userEmotionRepository.countTodaySalesByUser(user, ReviewStatus.APPROVED);
        if (todaySales >= DAILY_SALE_LIMIT) {
            throw new IllegalArgumentException("일일 감정 판매 제한을 초과했습니다 (5건/일)");
        }
    }

    private void validateEmotionData(EmotionType emotionType, Integer intensity, String story) {
        if (emotionType == null) {
            throw new IllegalArgumentException("감정 유형을 선택해주세요");
        }
        if (intensity == null || intensity < 1 || intensity > 10) {
            throw new IllegalArgumentException("감정 강도는 1-10 사이의 값이어야 합니다");
        }
        if (story == null || story.trim().length() < 10) {
            throw new IllegalArgumentException("감정 스토리는 최소 10자 이상 작성해주세요");
        }
        if (story.length() > 500) {
            throw new IllegalArgumentException("감정 스토리는 최대 500자까지 작성 가능합니다");
        }
    }

    private int calculatePoints(String story, boolean allowResale, boolean allowCreativeUse) {
        int points = BASE_POINTS;

        // 상세 스토리 보너스
        if (story != null && story.trim().length() >= DETAILED_STORY_MIN_LENGTH) {
            points += DETAILED_STORY_BONUS;
        }

        // 재사용 권한 보너스
        if (allowResale || allowCreativeUse) {
            points += REUSE_PERMISSION_BONUS;
        }

        return points;
    }

    private String createReusePermissionJson(boolean allowResale, boolean allowCreativeUse) {
        return String.format("{\"allowResale\": %b, \"allowCreativeUse\": %b}", 
                           allowResale, allowCreativeUse);
    }

    private String createBonusDetailsJson(String story, boolean allowResale, boolean allowCreativeUse) {
        StringBuilder bonusDetails = new StringBuilder("{\"basePoints\": " + BASE_POINTS);

        if (story != null && story.trim().length() >= DETAILED_STORY_MIN_LENGTH) {
            bonusDetails.append(", \"detailedStoryBonus\": ").append(DETAILED_STORY_BONUS);
        }

        if (allowResale || allowCreativeUse) {
            bonusDetails.append(", \"reusePermissionBonus\": ").append(REUSE_PERMISSION_BONUS);
        }

        bonusDetails.append("}");
        return bonusDetails.toString();
    }

    // 관리자용 메서드들
    @Transactional
    public void approveEmotion(Long emotionId, String reviewedBy) {
        UserEmotion emotion = userEmotionRepository.findById(emotionId)
                .orElseThrow(() -> new IllegalArgumentException("감정을 찾을 수 없습니다"));
        
        emotion.approve(reviewedBy);
        log.info("감정 승인: emotionId={}, reviewedBy={}", emotionId, reviewedBy);
    }

    @Transactional
    public void rejectEmotion(Long emotionId, String reviewedBy, String reason) {
        UserEmotion emotion = userEmotionRepository.findById(emotionId)
                .orElseThrow(() -> new IllegalArgumentException("감정을 찾을 수 없습니다"));
        
        emotion.reject(reviewedBy, reason);
        
        // 지급된 포인트 회수
        userService.subtractPoints(emotion.getUser().getId(), emotion.getPointsEarned());
        pointTransactionService.recordEmotionRefund(emotion.getUser(), emotion);
        
        log.info("감정 거부: emotionId={}, reviewedBy={}, reason={}", emotionId, reviewedBy, reason);
    }

    public List<UserEmotion> getPendingEmotions() {
        return userEmotionRepository.findByStatusOrderByCreatedAtAsc(ReviewStatus.PENDING);
    }

    public Long countPendingEmotions() {
        return userEmotionRepository.countByStatus(ReviewStatus.PENDING);
    }
}
