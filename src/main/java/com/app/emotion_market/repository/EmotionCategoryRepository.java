package com.app.emotion_market.repository;

import com.app.emotion_market.entity.EmotionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 감정 카테고리 Repository
 */
@Repository
public interface EmotionCategoryRepository extends JpaRepository<EmotionCategory, Long> {

    /**
     * 활성화된 카테고리들을 정렬 순서대로 조회
     */
    List<EmotionCategory> findByIsActiveTrueOrderBySortOrderAsc();

    /**
     * ID로 활성화된 카테고리 조회
     */
    Optional<EmotionCategory> findByIdAndIsActiveTrue(Long id);

    /**
     * 이름으로 카테고리 조회
     */
    Optional<EmotionCategory> findByName(String name);

    /**
     * 이름으로 활성화된 카테고리 조회
     */
    Optional<EmotionCategory> findByNameAndIsActiveTrue(String name);

    /**
     * 활성화된 카테고리 개수 조회
     */
    long countByIsActiveTrue();

    /**
     * 카테고리별 감정 상품 개수와 함께 조회
     */
    @Query("""
        SELECT c, COUNT(se.id) as emotionCount
        FROM EmotionCategory c 
        LEFT JOIN SystemEmotion se ON se.categoryId = c.id AND se.isActive = true
        WHERE c.isActive = true
        GROUP BY c.id
        ORDER BY c.sortOrder ASC
        """)
    List<Object[]> findCategoriesWithEmotionCount();

    /**
     * 카테고리별 평균 가격 조회
     */
    @Query("""
        SELECT AVG(se.price)
        FROM SystemEmotion se
        WHERE se.categoryId = :categoryId AND se.isActive = true
        """)
    Double findAveragePriceByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 카테고리별 총 구매 수 조회
     */
    @Query("""
        SELECT SUM(se.totalPurchases)
        FROM SystemEmotion se
        WHERE se.categoryId = :categoryId AND se.isActive = true
        """)
    Long findTotalPurchasesByCategoryId(@Param("categoryId") Long categoryId);
}
