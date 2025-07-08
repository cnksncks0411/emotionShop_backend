package com.app.emotion_market.repository.custom;

import com.app.emotion_market.entity.PointTransaction;
import com.app.emotion_market.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PointTransactionRepositoryCustom {
    
    Page<PointTransaction> findTransactionsWithFilters(Long userId, TransactionType transactionType,
                                                      LocalDateTime startDate, LocalDateTime endDate,
                                                      Pageable pageable);
    
    List<Object[]> getMonthlyTransactionStats(Long userId, int months);
    
    List<Object[]> getDailyTransactionStats(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    Integer getTotalEarnedByUser(Long userId);
    
    Integer getTotalSpentByUser(Long userId);
    
    Map<String, Integer> getEarningsByTransactionType(Long userId);
    
    Map<String, Integer> getSpendingByTransactionType(Long userId);
}
