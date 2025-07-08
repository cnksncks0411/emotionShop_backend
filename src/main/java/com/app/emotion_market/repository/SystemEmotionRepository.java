package com.app.emotion_market.repository;

import com.app.emotion_market.enumType.EmotionType;
import com.app.emotion_market.entity.SystemEmotion;
import com.app.emotion_market.repository.custom.SystemEmotionRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SystemEmotionRepository extends JpaRepository<SystemEmotion, Long>, SystemEmotionRepositoryCustom {

    List<SystemEmotion> findByIsActiveTrueOrderBySortOrderAscCreatedAtDesc();

    List<SystemEmotion> findByCategoryIdAndIsActiveTrueOrderBySortOrderAscCreatedAtDesc(Integer categoryId);

    List<SystemEmotion> findByEmotionTypeAndIsActiveTrueOrderByPriceAsc(EmotionType emotionType);

    Optional<SystemEmotion> findByIdAndIsActiveTrue(Long id);

    @Query("SELECT se FROM SystemEmotion se WHERE se.isActive = true ORDER BY se.totalPurchases DESC")
    List<SystemEmotion> findPopularEmotions(@Param("limit") int limit);

    @Query("SELECT se FROM SystemEmotion se WHERE se.isActive = true AND se.price BETWEEN :minPrice AND :maxPrice ORDER BY se.averageRating DESC")
    List<SystemEmotion> findByPriceRangeOrderByRating(@Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice);

    @Query("SELECT DISTINCT se.categoryId FROM SystemEmotion se WHERE se.isActive = true ORDER BY se.categoryId")
    List<Integer> findActiveCategoryIds();

    @Query("SELECT COUNT(se) FROM SystemEmotion se WHERE se.categoryId = :categoryId AND se.isActive = true")
    Long countByCategoryIdAndIsActiveTrue(@Param("categoryId") Integer categoryId);

    @Query("SELECT AVG(se.price) FROM SystemEmotion se WHERE se.categoryId = :categoryId AND se.isActive = true")
    Double getAveragePriceByCategoryId(@Param("categoryId") Integer categoryId);

    @Query("SELECT SUM(se.totalPurchases) FROM SystemEmotion se WHERE se.categoryId = :categoryId AND se.isActive = true")
    Long getTotalPurchasesByCategoryId(@Param("categoryId") Integer categoryId);

    // 카테고리별 감정 상품 조회 (페이징)
    @Query("SELECT se FROM SystemEmotion se WHERE se.categoryId = :categoryId AND se.isActive = true ORDER BY se.totalPurchases DESC")
    Page<SystemEmotion> findByCategoryIdAndIsActiveTrueOrderByTotalPurchasesDesc(@Param("categoryId") Integer categoryId, Pageable pageable);

    // 감정 유형별 상품 조회
    List<SystemEmotion> findByEmotionTypeAndIsActiveTrueOrderByTotalPurchasesDesc(EmotionType emotionType);

    // 인기 감정 TOP 10
    List<SystemEmotion> findTop10ByIsActiveTrueOrderByTotalPurchasesDesc();

    // 추천 감정 TOP 10 (평점순)
    List<SystemEmotion> findTop10ByIsActiveTrueOrderByAverageRatingDesc();

    // 이름으로 감정 검색
    List<SystemEmotion> findByNameContainingIgnoreCaseAndIsActiveTrueOrderByTotalPurchasesDesc(String keyword);

    // 가격 범위로 감정 검색
    List<SystemEmotion> findByPriceBetweenAndIsActiveTrueOrderByPriceAsc(Integer minPrice, Integer maxPrice);

    // 평점 이상으로 감정 검색
    List<SystemEmotion> findByAverageRatingGreaterThanEqualAndIsActiveTrueOrderByAverageRatingDesc(BigDecimal minRating);

    // 활성화된 감정들 정렬순으로 조회
    List<SystemEmotion> findByIsActiveTrueOrderBySortOrderAsc();

    // 구매 수 증가
    @Modifying
    @Query("UPDATE SystemEmotion se SET se.totalPurchases = se.totalPurchases + 1 WHERE se.id = :emotionId")
    void incrementTotalPurchases(@Param("emotionId") Long emotionId);

    // 평점 업데이트
    @Modifying
    @Query("UPDATE SystemEmotion se SET se.averageRating = :newRating WHERE se.id = :emotionId")
    void updateAverageRating(@Param("emotionId") Long emotionId, @Param("newRating") Double newRating);
}
