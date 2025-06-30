package com.app.emotion_market.repository;

import com.app.emotion_market.entity.Purchase;
import com.app.emotion_market.entity.PurchaseStatus;
import com.app.emotion_market.entity.QPurchase;
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
public class PurchaseRepositoryImpl implements PurchaseRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Purchase> findPurchasesWithFilters(Long userId, PurchaseStatus status,
                                                   LocalDateTime startDate, LocalDateTime endDate,
                                                   Pageable pageable) {
        QPurchase purchase = QPurchase.purchase;

        BooleanExpression condition = purchase.isNotNull();

        if (userId != null) {
            condition = condition.and(purchase.user.id.eq(userId));
        }

        if (status != null) {
            condition = condition.and(purchase.status.eq(status));
        }

        if (startDate != null) {
            condition = condition.and(purchase.createdAt.goe(startDate));
        }

        if (endDate != null) {
            condition = condition.and(purchase.createdAt.loe(endDate));
        }

        List<Purchase> content = queryFactory
                .selectFrom(purchase)
                .leftJoin(purchase.user).fetchJoin()
                .leftJoin(purchase.emotion).fetchJoin()
                .where(condition)
                .orderBy(purchase.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(purchase.count())
                .from(purchase)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Purchase> findExpiredPurchases() {
        QPurchase purchase = QPurchase.purchase;

        return queryFactory
                .selectFrom(purchase)
                .where(
                    purchase.status.eq(PurchaseStatus.ACTIVE)
                    .and(purchase.expiresAt.before(LocalDateTime.now()))
                )
                .fetch();
    }

    @Override
    public List<Purchase> findPurchasesExpiringInDays(int days) {
        QPurchase purchase = QPurchase.purchase;
        LocalDateTime futureDate = LocalDateTime.now().plusDays(days);

        return queryFactory
                .selectFrom(purchase)
                .leftJoin(purchase.user).fetchJoin()
                .leftJoin(purchase.emotion).fetchJoin()
                .where(
                    purchase.status.eq(PurchaseStatus.ACTIVE)
                    .and(purchase.expiresAt.between(LocalDateTime.now(), futureDate))
                )
                .orderBy(purchase.expiresAt.asc())
                .fetch();
    }

    @Override
    public List<Object[]> getPurchaseStatsByUser(Long userId) {
        QPurchase purchase = QPurchase.purchase;

        return queryFactory
                .select(
                    purchase.emotion.emotionType,
                    purchase.count(),
                    purchase.pointsSpent.sum(),
                    purchase.accessCount.avg()
                )
                .from(purchase)
                .where(purchase.user.id.eq(userId))
                .groupBy(purchase.emotion.emotionType)
                .orderBy(purchase.count().desc())
                .fetch();
    }

    @Override
    public List<Object[]> getPopularEmotionStats(int limit) {
        QPurchase purchase = QPurchase.purchase;

        return queryFactory
                .select(
                    purchase.emotion.id,
                    purchase.emotion.name,
                    purchase.count(),
                    purchase.rating.avg()
                )
                .from(purchase)
                .where(purchase.status.eq(PurchaseStatus.ACTIVE))
                .groupBy(purchase.emotion.id, purchase.emotion.name)
                .orderBy(purchase.count().desc())
                .limit(limit)
                .fetch();
    }
}
