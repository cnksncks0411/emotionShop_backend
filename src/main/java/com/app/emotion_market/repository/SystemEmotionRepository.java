package com.app.emotion_market.repository;

import com.app.emotion_market.entity.EmotionType;
import com.app.emotion_market.entity.SystemEmotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
