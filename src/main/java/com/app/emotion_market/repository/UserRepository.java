package com.app.emotion_market.repository;

import com.app.emotion_market.entity.User;
import com.app.emotion_market.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    Long countNewUsersAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(u.points) FROM User u WHERE u.status = :status")
    Long getTotalPointsByStatus(@Param("status") UserStatus status);
}
