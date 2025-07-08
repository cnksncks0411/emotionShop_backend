package com.app.emotion_market.repository.impl;

import com.app.emotion_market.entity.PointTransaction;
import com.app.emotion_market.enums.TransactionType;
import com.app.emotion_market.repository.custom.PointTransactionRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
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
public class PointTransactionRepositoryImpl implements PointTransactionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PointTransaction> findTransactionsWithFilters(Long userId, TransactionType transactionType,
                                                              LocalDateTime startDate, LocalDateTime endDate,
                                                              Pageable pageable) {
        QPointTransaction pointTransaction = QPointTransaction.pointTransaction;

        BooleanExpression condition = pointTransaction.isNotNull();

        if (userId != null) {
            condition = condition.and(pointTransaction.user.id.eq(userId));
        }

        if (transactionType != null) {
            condition = condition.and(pointTransaction.transactionType.eq(transactionType));
        }

        if (startDate != null) {
            condition = condition.and(pointTransaction.createdAt.goe(startDate));
        }

        if (endDate != null) {
            condition = condition.and(pointTransaction.createdAt.loe(endDate));
        }

        List<PointTransaction> content = queryFactory
                .selectFrom(pointTransaction)
                .leftJoin(pointTransaction.user).fetchJoin()
                .where(condition)
                .orderBy(pointTransaction.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(pointTransaction.count())
                .from(pointTransaction)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Object[]> getMonthlyTransactionStats(Long userId, int months) {
        QPointTransaction pointTransaction = QPointTransaction.pointTransaction;
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);

        return queryFactory
                .select(
                    Expressions.stringTemplate("DATE_TRUNC('month', {0})", pointTransaction.createdAt),
                    pointTransaction.transactionType,
                    pointTransaction.amount.sum(),
                    pointTransaction.count()
                )
                .from(pointTransaction)
                .where(
                    pointTransaction.user.id.eq(userId)
                    .and(pointTransaction.createdAt.goe(startDate))
                )
                .groupBy(
                    Expressions.stringTemplate("DATE_TRUNC('month', {0})", pointTransaction.createdAt),
                    pointTransaction.transactionType
                )
                .orderBy(Expressions.stringTemplate("DATE_TRUNC('month', {0})", pointTransaction.createdAt).asc())
                .fetch();
    }

    @Override
    public List<Object[]> getDailyTransactionStats(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        QPointTransaction pointTransaction = QPointTransaction.pointTransaction;

        return queryFactory
                .select(
                    Expressions.stringTemplate("DATE_TRUNC('day', {0})", pointTransaction.createdAt),
                    pointTransaction.transactionType,
                    pointTransaction.amount.sum(),
                    pointTransaction.count()
                )
                .from(pointTransaction)
                .where(
                    pointTransaction.user.id.eq(userId)
                    .and(pointTransaction.createdAt.between(startDate, endDate))
                )
                .groupBy(
                    Expressions.stringTemplate("DATE_TRUNC('day', {0})", pointTransaction.createdAt),
                    pointTransaction.transactionType
                )
                .orderBy(Expressions.stringTemplate("DATE_TRUNC('day', {0})", pointTransaction.createdAt).asc())
                .fetch();
    }

    @Override
    public Integer getTotalEarnedByUser(Long userId) {
        QPointTransaction pointTransaction = QPointTransaction.pointTransaction;

        Integer result = queryFactory
                .select(pointTransaction.amount.sum())
                .from(pointTransaction)
                .where(
                    pointTransaction.user.id.eq(userId)
                    .and(pointTransaction.amount.gt(0)) // 양수만 (획득)
                )
                .fetchOne();

        return result != null ? result : 0;
    }

    @Override
    public Integer getTotalSpentByUser(Long userId) {
        QPointTransaction pointTransaction = QPointTransaction.pointTransaction;

        Integer result = queryFactory
                .select(pointTransaction.amount.sum())
                .from(pointTransaction)
                .where(
                    pointTransaction.user.id.eq(userId)
                    .and(pointTransaction.amount.lt(0)) // 음수만 (사용)
                )
                .fetchOne();

        return result != null ? Math.abs(result) : 0; // 절댓값으로 반환
    }

    @Override
    public java.util.Map<String, Integer> getEarningsByTransactionType(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        QPointTransaction pointTransaction = QPointTransaction.pointTransaction;

        List<Object[]> results = queryFactory
                .select(
                    pointTransaction.transactionType,
                    pointTransaction.amount.sum()
                )
                .from(pointTransaction)
                .where(
                    pointTransaction.user.id.eq(userId)
                    .and(pointTransaction.amount.gt(0)) // 양수만 (획득)
                    .and(pointTransaction.createdAt.between(startDate, endDate))
                )
                .groupBy(pointTransaction.transactionType)
                .fetch();

        java.util.Map<String, Integer> earnings = new java.util.HashMap<>();
        for (Object[] result : results) {
            TransactionType type = (TransactionType) result[0];
            Integer amount = (Integer) result[1];
            earnings.put(type.name(), amount != null ? amount : 0);
        }

        return earnings;
    }

    @Override
    public java.util.Map<String, Integer> getSpendingByTransactionType(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        QPointTransaction pointTransaction = QPointTransaction.pointTransaction;

        List<Object[]> results = queryFactory
                .select(
                    pointTransaction.transactionType,
                    pointTransaction.amount.sum()
                )
                .from(pointTransaction)
                .where(
                    pointTransaction.user.id.eq(userId)
                    .and(pointTransaction.amount.lt(0)) // 음수만 (사용)
                    .and(pointTransaction.createdAt.between(startDate, endDate))
                )
                .groupBy(pointTransaction.transactionType)
                .fetch();

        java.util.Map<String, Integer> spending = new java.util.HashMap<>();
        for (Object[] result : results) {
            TransactionType type = (TransactionType) result[0];
            Integer amount = (Integer) result[1];
            spending.put(type.name(), amount != null ? Math.abs(amount) : 0); // 절댓값으로 반환
        }

        return spending;
    }
}
