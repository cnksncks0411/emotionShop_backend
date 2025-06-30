package com.app.emotion_market.repository;

import com.app.emotion_market.entity.EmotionType;
import com.app.emotion_market.entity.SystemEmotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface SystemEmotionRepositoryCustom {
    
    Page<SystemEmotion> findEmotionsWithFilters(Integer categoryId, EmotionType emotionType,
                                               Integer minPrice, Integer maxPrice,
                                               BigDecimal minRating, String sortBy,
                                               Pageable pageable);
    
    List<SystemEmotion> findRecommendedEmotions(Long userId, int limit);
    
    List<SystemEmotion> searchEmotionsByKeyword(String keyword);
    
    List<Object[]> getEmotionStatsByCategory();
}
