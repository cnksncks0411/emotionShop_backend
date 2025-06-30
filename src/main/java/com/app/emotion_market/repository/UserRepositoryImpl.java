package com.app.emotion_market.repository;

import com.app.emotion_market.entity.QUser;
import com.app.emotion_market.entity.User;
import com.app.emotion_market.entity.UserStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<User> findUsersWithActivity(Pageable pageable) {
        QUser user = QUser.user;

        List<User> content = queryFactory
                .selectFrom(user)
                .where(user.status.eq(UserStatus.ACTIVE))
                .orderBy(user.lastLoginAt.desc().nullsLast(), user.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(user.count())
                .from(user)
                .where(user.status.eq(UserStatus.ACTIVE))
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<User> searchUsersByKeyword(String keyword, Pageable pageable) {
        QUser user = QUser.user;

        BooleanExpression condition = user.status.eq(UserStatus.ACTIVE);

        if (StringUtils.hasText(keyword)) {
            condition = condition.and(
                user.email.containsIgnoreCase(keyword)
                .or(user.nickname.containsIgnoreCase(keyword))
            );
        }

        List<User> content = queryFactory
                .selectFrom(user)
                .where(condition)
                .orderBy(user.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(user.count())
                .from(user)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Long countActiveUsersInPeriod(LocalDateTime start, LocalDateTime end) {
        QUser user = QUser.user;

        return queryFactory
                .select(user.count())
                .from(user)
                .where(
                    user.status.eq(UserStatus.ACTIVE)
                    .and(user.lastLoginAt.between(start, end))
                )
                .fetchOne();
    }
}
