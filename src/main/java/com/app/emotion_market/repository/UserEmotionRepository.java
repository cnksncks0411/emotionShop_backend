package com.app.emotion_market.repository;

import com.app.emotion_market.enumType.EmotionType;
import com.app.emotion_market.enumType.ReviewStatus;
import com.app.emotion_market.entity.User;
import com.app.emotion_market.entity.UserEmotion;
import com.app.emotion_market.repository.custom.UserEmotionRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserEmotionRepository extends JpaRepository<UserEmotion, Long>, UserEmotionRepositoryCustom {

    @Query("SELECT COUNT(ue) FROM UserEmotion ue WHERE ue.user = :user AND DATE(ue.createdAt) = CURRENT_DATE AND ue.status = :status")
    Long countTodaySalesByUser(@Param("user") User user, @Param("status") ReviewStatus status);

    @Query("SELECT COUNT(ue) FROM UserEmotion ue WHERE ue.user.id = :userId AND ue.createdAt >= :startDate AND ue.status = :status")
    Long countByUserIdAndCreatedAtAfterAndStatus(@Param("userId") Long userId, 
                                                @Param("startDate") LocalDateTime startDate, 
                                                @Param("status") ReviewStatus status);

    List<UserEmotion> findByUserAndStatusOrderByCreatedAtDesc(User user, ReviewStatus status);

    List<UserEmotion> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT ue FROM UserEmotion ue WHERE ue.user = :user AND ue.emotionType = :emotionType AND ue.status = :status ORDER BY ue.createdAt DESC")
    List<UserEmotion> findByUserAndEmotionTypeAndStatus(@Param("user") User user, 
                                                        @Param("emotionType") EmotionType emotionType, 
                                                        @Param("status") ReviewStatus status);

    @Query("SELECT COUNT(ue) FROM UserEmotion ue WHERE ue.status = :status")
    Long countByStatus(@Param("status") ReviewStatus status);

    @Query("SELECT ue FROM UserEmotion ue WHERE ue.status = :status ORDER BY ue.createdAt ASC")
    List<UserEmotion> findByStatusOrderByCreatedAtAsc(@Param("status") ReviewStatus status);
}
