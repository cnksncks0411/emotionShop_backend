package com.app.emotion_market.service;

import com.app.emotion_market.entity.EmotionCategory;
import com.app.emotion_market.repository.EmotionCategoryRepository;
import com.app.emotion_market.repository.SystemEmotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 감정 카테고리 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmotionCategoryService {

    private final EmotionCategoryRepository emotionCategoryRepository;
    private final SystemEmotionRepository systemEmotionRepository;

    /**
     * 모든 활성 카테고리 조회 (정렬 순서대로)
     */
    public List<EmotionCategory> findAllActiveCategories() {
        return emotionCategoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    /**
     * 카테고리 상세 조회
     */
    public Optional<EmotionCategory> findCategoryById(Long categoryId) {
        return emotionCategoryRepository.findByIdAndIsActiveTrue(categoryId);
    }

    /**
     * 카테고리별 감정 상품 개수 조회
     */
    public Long countEmotionsByCategory(Long categoryId) {
        return systemEmotionRepository.countByCategoryIdAndIsActiveTrue(categoryId.intValue());
    }

    /**
     * 카테고리별 평균 가격 조회
     */
    public Double getAveragePriceByCategory(Long categoryId) {
        return systemEmotionRepository.getAveragePriceByCategoryId(categoryId.intValue());
    }

    /**
     * 카테고리별 인기도 조회 (총 구매수 기준)
     */
    public Long getTotalPurchasesByCategory(Long categoryId) {
        return systemEmotionRepository.getTotalPurchasesByCategoryId(categoryId.intValue());
    }

    /**
     * 카테고리 통계 정보와 함께 조회
     */
    public List<CategoryWithStats> findCategoriesWithStats() {
        List<EmotionCategory> categories = findAllActiveCategories();
        
        return categories.stream()
                .map(category -> {
                    Long emotionCount = countEmotionsByCategory(category.getId());
                    Double averagePrice = getAveragePriceByCategory(category.getId());
                    Long totalPurchases = getTotalPurchasesByCategory(category.getId());
                    
                    return CategoryWithStats.builder()
                            .category(category)
                            .emotionCount(emotionCount)
                            .averagePrice(averagePrice != null ? averagePrice : 0.0)
                            .totalPurchases(totalPurchases != null ? totalPurchases : 0L)
                            .build();
                })
                .toList();
    }

    /**
     * 관리자용: 카테고리 생성
     */
    @Transactional
    public EmotionCategory createCategory(String name, String description, String colorCode, Integer sortOrder) {
        EmotionCategory category = EmotionCategory.builder()
                .name(name)
                .description(description)
                .colorCode(colorCode)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .isActive(true)
                .build();

        category = emotionCategoryRepository.save(category);
        log.info("새 카테고리 생성: id={}, name={}", category.getId(), category.getName());
        
        return category;
    }

    /**
     * 관리자용: 카테고리 수정
     */
    @Transactional
    public EmotionCategory updateCategory(Long categoryId, String name, String description, 
                                        String colorCode, Integer sortOrder) {
        EmotionCategory category = emotionCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다"));

        if (name != null) category.setName(name);
        if (description != null) category.setDescription(description);
        if (colorCode != null) category.setColorCode(colorCode);
        if (sortOrder != null) category.setSortOrder(sortOrder);

        log.info("카테고리 수정: id={}, name={}", category.getId(), category.getName());
        
        return category;
    }

    /**
     * 관리자용: 카테고리 활성화/비활성화
     */
    @Transactional
    public void toggleCategoryStatus(Long categoryId) {
        EmotionCategory category = emotionCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다"));

        category.setIsActive(!category.getIsActive());
        log.info("카테고리 상태 변경: id={}, isActive={}", category.getId(), category.getIsActive());
    }

    /**
     * 카테고리 통계 DTO
     */
    public static class CategoryWithStats {
        private final EmotionCategory category;
        private final Long emotionCount;
        private final Double averagePrice;
        private final Long totalPurchases;

        public CategoryWithStats(EmotionCategory category, Long emotionCount, Double averagePrice, Long totalPurchases) {
            this.category = category;
            this.emotionCount = emotionCount;
            this.averagePrice = averagePrice;
            this.totalPurchases = totalPurchases;
        }

        public static CategoryWithStatsBuilder builder() {
            return new CategoryWithStatsBuilder();
        }

        public EmotionCategory getCategory() { return category; }
        public Long getEmotionCount() { return emotionCount; }
        public Double getAveragePrice() { return averagePrice; }
        public Long getTotalPurchases() { return totalPurchases; }

        public static class CategoryWithStatsBuilder {
            private EmotionCategory category;
            private Long emotionCount;
            private Double averagePrice;
            private Long totalPurchases;

            public CategoryWithStatsBuilder category(EmotionCategory category) {
                this.category = category;
                return this;
            }

            public CategoryWithStatsBuilder emotionCount(Long emotionCount) {
                this.emotionCount = emotionCount;
                return this;
            }

            public CategoryWithStatsBuilder averagePrice(Double averagePrice) {
                this.averagePrice = averagePrice;
                return this;
            }

            public CategoryWithStatsBuilder totalPurchases(Long totalPurchases) {
                this.totalPurchases = totalPurchases;
                return this;
            }

            public CategoryWithStats build() {
                return new CategoryWithStats(category, emotionCount, averagePrice, totalPurchases);
            }
        }
    }
}
