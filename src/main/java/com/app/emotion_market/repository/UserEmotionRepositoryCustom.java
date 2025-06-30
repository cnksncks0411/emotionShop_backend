package com.app.emotion_market.repository;

import com.app.emotion_market.entity.EmotionType;
import com.app.emotion_market.entity.UserEmotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface UserEmotionRepositoryCustom {
    
    Page<UserEmotion> findUserEmotionsWithFilters(Long userId, EmotionType emotionType, 
                                                   LocalDateTime startDate, LocalDateTime endDate, 
                                                   Pageable pageable);
    
    List<UserEmotion> findTopEmotionsByUser(Long userId, int limit);
    
    Long countEmotionsSoldByUserInPeriod(Long userId, LocalDateTime start, LocalDateTime end);
    
    List<Object[]> getEmotionStatsByUser(Long userId);
}
