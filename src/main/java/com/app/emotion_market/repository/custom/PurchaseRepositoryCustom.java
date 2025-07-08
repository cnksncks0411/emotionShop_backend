package com.app.emotion_market.repository.custom;

import com.app.emotion_market.entity.Purchase;
import com.app.emotion_market.enums.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseRepositoryCustom {
    
    Page<Purchase> findPurchasesWithFilters(Long userId, PurchaseStatus status,
                                           LocalDateTime startDate, LocalDateTime endDate,
                                           Pageable pageable);
    
    List<Purchase> findExpiredPurchases();
    
    List<Purchase> findPurchasesExpiringInDays(int days);
    
    List<Object[]> getPurchaseStatsByUser(Long userId);
    
    List<Object[]> getPopularEmotionStats(int limit);
}
