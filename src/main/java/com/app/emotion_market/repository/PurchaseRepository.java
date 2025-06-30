package com.app.emotion_market.repository;

import com.app.emotion_market.entity.Purchase;
import com.app.emotion_market.entity.PurchaseStatus;
import com.app.emotion_market.entity.SystemEmotion;
import com.app.emotion_market.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long>, PurchaseRepositoryCustom {

    List<Purchase> findByUserOrderByCreatedAtDesc(User user);

    List<Purchase> findByUserAndStatusOrderByCreatedAtDesc(User user, PurchaseStatus status);

    Optional<Purchase> findByUserAndEmotionAndStatus(User user, SystemEmotion emotion, PurchaseStatus status);

    @Query("SELECT COUNT(p) FROM Purchase p WHERE p.user = :user AND p.emotion = :emotion AND p.status = :status AND p.createdAt >= :startDate")
    Long countRecentPurchases(@Param("user") User user, 
                             @Param("emotion") SystemEmotion emotion, 
                             @Param("status") PurchaseStatus status, 
                             @Param("startDate") LocalDateTime startDate);

    @Query("SELECT p FROM Purchase p WHERE p.status = :status AND p.expiresAt <= :expiryDate")
    List<Purchase> findExpiringPurchases(@Param("status") PurchaseStatus status, 
                                        @Param("expiryDate") LocalDateTime expiryDate);

    @Query("SELECT p FROM Purchase p WHERE p.user = :user AND p.status = :status AND p.expiresAt > CURRENT_TIMESTAMP")
    List<Purchase> findActivePurchasesByUser(@Param("user") User user, @Param("status") PurchaseStatus status);

    @Query("SELECT COUNT(p) FROM Purchase p WHERE p.emotion = :emotion AND p.status = :status")
    Long countByEmotionAndStatus(@Param("emotion") SystemEmotion emotion, @Param("status") PurchaseStatus status);

    @Query("SELECT SUM(p.pointsSpent) FROM Purchase p WHERE p.user = :user AND p.status = :status")
    Integer getTotalSpentByUser(@Param("user") User user, @Param("status") PurchaseStatus status);
}
