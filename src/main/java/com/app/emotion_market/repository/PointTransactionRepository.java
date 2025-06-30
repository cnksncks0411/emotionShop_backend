package com.app.emotion_market.repository;

import com.app.emotion_market.entity.PointTransaction;
import com.app.emotion_market.entity.TransactionType;
import com.app.emotion_market.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long>, PointTransactionRepositoryCustom {

    List<PointTransaction> findByUserOrderByCreatedAtDesc(User user);

    List<PointTransaction> findByUserAndTransactionTypeOrderByCreatedAtDesc(User user, TransactionType transactionType);

    @Query("SELECT SUM(pt.amount) FROM PointTransaction pt WHERE pt.user = :user AND pt.transactionType = :type")
    Integer sumAmountByUserAndType(@Param("user") User user, @Param("type") TransactionType type);

    @Query("SELECT SUM(pt.amount) FROM PointTransaction pt WHERE pt.user = :user AND pt.createdAt BETWEEN :startDate AND :endDate")
    Integer sumAmountByUserInPeriod(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT pt FROM PointTransaction pt WHERE pt.user = :user AND pt.createdAt >= :startDate ORDER BY pt.createdAt DESC")
    List<PointTransaction> findRecentTransactionsByUser(@Param("user") User user, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(pt) FROM PointTransaction pt WHERE pt.transactionType = :type AND pt.createdAt >= :startDate")
    Long countByTypeAfterDate(@Param("type") TransactionType type, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(pt.amount) FROM PointTransaction pt WHERE pt.transactionType = :type AND pt.createdAt BETWEEN :startDate AND :endDate")
    Integer sumAmountByTypeInPeriod(@Param("type") TransactionType type, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
