package com.app.emotion_market.repository;

import com.app.emotion_market.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface UserRepositoryCustom {
    
    Page<User> findUsersWithActivity(Pageable pageable);
    
    Page<User> searchUsersByKeyword(String keyword, Pageable pageable);
    
    Long countActiveUsersInPeriod(LocalDateTime start, LocalDateTime end);
}
