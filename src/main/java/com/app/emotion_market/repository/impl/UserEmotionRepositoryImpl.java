package com.app.emotion_market.repository.impl;

import com.app.emotion_market.entity.*;
import com.app.emotion_market.enumType.EmotionType;
import com.app.emotion_market.enumType.ReviewStatus;
import com.app.emotion_market.repository.custom.UserEmotionRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserEmotionRepositoryImpl implements UserEmotionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<UserEmotion> findUserEmotionsWithFilters(Long userId, EmotionType emotionType,
                                                          LocalDateTime startDate, LocalDateTime endDate,
                                                          Pageable pageable) {
        QUserEmotion userEmotion = QUserEmotion.userEmotion;

        BooleanExpression condition = userEmotion.status.eq(ReviewStatus.APPROVED);

        if (userId != null) {
            condition = condition.and(userEmotion.user.id.eq(userId));
        }

        if (emotionType != null) {
            condition = condition.and(userEmotion.emotionType.eq(emotionType));
        }

        if (startDate != null) {
            condition = condition.and(userEmotion.createdAt.goe(startDate));
        }

        if (endDate != null) {
            condition = condition.and(userEmotion.createdAt.loe(endDate));
        }

        List<UserEmotion> content = queryFactory
                .selectFrom(userEmotion)
                .where(condition)
                .orderBy(userEmotion.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(userEmotion.count())
                .from(userEmotion)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<UserEmotion> findTopEmotionsByUser(Long userId, int limit) {
        QUserEmotion userEmotion = QUserEmotion.userEmotion;

        return queryFactory
                .selectFrom(userEmotion)
                .where(
                    userEmotion.user.id.eq(userId)
                    .and(userEmotion.status.eq(ReviewStatus.APPROVED))
                )
                .orderBy(userEmotion.pointsEarned.desc(), userEmotion.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Long countEmotionsSoldByUserInPeriod(Long userId, LocalDateTime start, LocalDateTime end) {
        QUserEmotion userEmotion = QUserEmotion.userEmotion;

        return queryFactory
                .select(userEmotion.count())
                .from(userEmotion)
                .where(
                    userEmotion.user.id.eq(userId)
                    .and(userEmotion.status.eq(ReviewStatus.APPROVED))
                    .and(userEmotion.createdAt.between(start, end))
                )
                .fetchOne();
    }

    @Override
    public List<Object[]> getEmotionStatsByUser(Long userId) {
        QUserEmotion userEmotion = QUserEmotion.userEmotion;

        return queryFactory
                .select(
                    userEmotion.emotionType,
                    userEmotion.count(),
                    userEmotion.pointsEarned.sum()
                )
                .from(userEmotion)
                .where(
                    userEmotion.user.id.eq(userId)
                    .and(userEmotion.status.eq(ReviewStatus.APPROVED))
                )
                .groupBy(userEmotion.emotionType)
                .orderBy(userEmotion.count().desc())
                .fetch();
    }
}
