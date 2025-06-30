package com.app.emotion_market.repository;

import com.app.emotion_market.entity.PointTransaction;
import com.app.emotion_market.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface PointTransactionRepositoryCustom {
    
    Page<PointTransaction> findTransactionsWithFilters(Long userId, TransactionType transactionType,
                                                       LocalDateTime startDate, LocalDateTime endDate,
                                                       Pageable pageable);
    
    List<Object[]> getMonthlyTransactionStats(Long userId, int months);
    
    List<Object[]> getDailyTransactionStats(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    Integer getTotalEarnedByUser(Long userId);
    
    Integer getTotalSpentByUser(Long userId);
}
