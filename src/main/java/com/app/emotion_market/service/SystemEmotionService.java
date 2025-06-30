package com.app.emotion_market.service;

import com.emotionshop.entity_market.common.enums.EmotionType;
import com.emotionshop.entity_market.entity.SystemEmotion;
import com.emotionshop.entity_market.repository.SystemEmotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 시스템 감정 상품 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SystemEmotionService {

    private final SystemEmotionRepository systemEmotionRepository;

    /**
     * 활성화된 모든 감정 상품 조회
     */
    public List<SystemEmotion> findAllActiveEmotions() {
        return systemEmotionRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    /**
     * 카테고리별 감정 상품 조회 (페이징)
     */
    public Page<SystemEmotion> findEmotionsByCategory(Long categoryId, Pageable pageable) {
        return systemEmotionRepository.findByCategoryIdAndIsActiveTrueOrderByTotalPurchasesDesc(categoryId, pageable);
    }

    /**
     * 감정 유형별 상품 조회
     */
    public List<SystemEmotion> findEmotionsByType(EmotionType emotionType) {
        return systemEmotionRepository.findByEmotionTypeAndIsActiveTrueOrderByTotalPurchasesDesc(emotionType);
    }

    /**
     * 인기 감정 상품 조회 (구매수 기준 TOP N)
     */
    public List<SystemEmotion> findPopularEmotions(int limit) {
        return systemEmotionRepository.findTop10ByIsActiveTrueOrderByTotalPurchasesDesc()
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * 추천 감정 상품 조회 (평점 기준)
     */
    public List<SystemEmotion> findRecommendedEmotions(int limit) {
        return systemEmotionRepository.findTop10ByIsActiveTrueOrderByAverageRatingDesc()
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * 감정 상품 상세 조회
     */
    public Optional<SystemEmotion> findEmotionById(Long emotionId) {
        return systemEmotionRepository.findByIdAndIsActiveTrue(emotionId);
    }

    /**
     * 감정 이름으로 검색
     */
    public List<SystemEmotion> searchEmotionsByName(String keyword) {
        return systemEmotionRepository.findByNameContainingIgnoreCaseAndIsActiveTrueOrderByTotalPurchasesDesc(keyword);
    }

    /**
     * 가격 범위로 필터링
     */
    public List<SystemEmotion> findEmotionsByPriceRange(Integer minPrice, Integer maxPrice) {
        if (minPrice == null) minPrice = 0;
        if (maxPrice == null) maxPrice = Integer.MAX_VALUE;
        
        return systemEmotionRepository.findByPriceBetweenAndIsActiveTrueOrderByPriceAsc(minPrice, maxPrice);
    }

    /**
     * 평점 이상으로 필터링
     */
    public List<SystemEmotion> findEmotionsByMinRating(Double minRating) {
        return systemEmotionRepository.findByAverageRatingGreaterThanEqualAndIsActiveTrueOrderByAverageRatingDesc(minRating);
    }

    /**
     * 감정 상품 구매 수 증가 (구매 시 호출)
     */
    @Transactional
    public void incrementPurchaseCount(Long emotionId) {
        systemEmotionRepository.incrementTotalPurchases(emotionId);
        log.info("감정 상품 구매 수 증가: emotionId={}", emotionId);
    }

    /**
     * 감정 상품 평점 업데이트
     */
    @Transactional
    public void updateAverageRating(Long emotionId, Double newRating) {
        systemEmotionRepository.updateAverageRating(emotionId, newRating);
        log.info("감정 상품 평점 업데이트: emotionId={}, newRating={}", emotionId, newRating);
    }

    /**
     * 오늘의 베스트 감정 조회 (최근 24시간 구매 기준)
     */
    public List<SystemEmotion> findTodaysBestEmotions(int limit) {
        // 실제로는 최근 24시간 구매 데이터를 기반으로 계산해야 하지만
        // MVP에서는 단순히 총 구매수 기준으로 처리
        return findPopularEmotions(limit);
    }

    /**
     * 관리자용: 감정 상품 활성화/비활성화
     */
    @Transactional
    public void toggleEmotionStatus(Long emotionId) {
        SystemEmotion emotion = systemEmotionRepository.findById(emotionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 감정 상품입니다"));
        
        emotion.setIsActive(!emotion.getIsActive());
        log.info("감정 상품 상태 변경: emotionId={}, isActive={}", emotionId, emotion.getIsActive());
    }

    /**
     * 관리자용: 감정 상품 가격 수정
     */
    @Transactional
    public void updateEmotionPrice(Long emotionId, Integer newPrice) {
        SystemEmotion emotion = systemEmotionRepository.findById(emotionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 감정 상품입니다"));
        
        emotion.setPrice(newPrice);
        log.info("감정 상품 가격 수정: emotionId={}, newPrice={}", emotionId, newPrice);
    }
}
