package com.app.emotion_market.repository;

import com.app.emotion_market.entity.*;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SystemEmotionRepositoryImpl implements SystemEmotionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SystemEmotion> findEmotionsWithFilters(Integer categoryId, EmotionType emotionType,
                                                        Integer minPrice, Integer maxPrice,
                                                        BigDecimal minRating, String sortBy,
                                                        Pageable pageable) {
        QSystemEmotion systemEmotion = QSystemEmotion.systemEmotion;

        BooleanExpression condition = systemEmotion.isActive.isTrue();

        if (categoryId != null) {
            condition = condition.and(systemEmotion.categoryId.eq(categoryId));
        }

        if (emotionType != null) {
            condition = condition.and(systemEmotion.emotionType.eq(emotionType));
        }

        if (minPrice != null) {
            condition = condition.and(systemEmotion.price.goe(minPrice));
        }

        if (maxPrice != null) {
            condition = condition.and(systemEmotion.price.loe(maxPrice));
        }

        if (minRating != null) {
            condition = condition.and(systemEmotion.averageRating.goe(minRating));
        }

        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(systemEmotion, sortBy);

        List<SystemEmotion> content = queryFactory
                .selectFrom(systemEmotion)
                .where(condition)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(systemEmotion.count())
                .from(systemEmotion)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<SystemEmotion> findRecommendedEmotions(Long userId, int limit) {
        QSystemEmotion systemEmotion = QSystemEmotion.systemEmotion;

        // 현재는 인기순으로 추천 (추후 개인화 로직 추가)
        return queryFactory
                .selectFrom(systemEmotion)
                .where(systemEmotion.isActive.isTrue())
                .orderBy(
                    systemEmotion.totalPurchases.desc(),
                    systemEmotion.averageRating.desc()
                )
                .limit(limit)
                .fetch();
    }

    @Override
    public List<SystemEmotion> searchEmotionsByKeyword(String keyword) {
        QSystemEmotion systemEmotion = QSystemEmotion.systemEmotion;

        BooleanExpression condition = systemEmotion.isActive.isTrue();

        if (StringUtils.hasText(keyword)) {
            condition = condition.and(
                systemEmotion.name.containsIgnoreCase(keyword)
                .or(systemEmotion.description.containsIgnoreCase(keyword))
            );
        }

        return queryFactory
                .selectFrom(systemEmotion)
                .where(condition)
                .orderBy(systemEmotion.totalPurchases.desc())
                .fetch();
    }

    @Override
    public List<Object[]> getEmotionStatsByCategory() {
        QSystemEmotion systemEmotion = QSystemEmotion.systemEmotion;

        return queryFactory
                .select(
                    systemEmotion.categoryId,
                    systemEmotion.count(),
                    systemEmotion.totalPurchases.sum(),
                    systemEmotion.averageRating.avg()
                )
                .from(systemEmotion)
                .where(systemEmotion.isActive.isTrue())
                .groupBy(systemEmotion.categoryId)
                .orderBy(systemEmotion.categoryId.asc())
                .fetch();
    }

    private OrderSpecifier<?> getOrderSpecifier(QSystemEmotion systemEmotion, String sortBy) {
        if (sortBy == null) {
            return systemEmotion.sortOrder.asc();
        }

        return switch (sortBy.toLowerCase()) {
            case "popularity" -> systemEmotion.totalPurchases.desc();
            case "rating" -> systemEmotion.averageRating.desc();
            case "price_asc" -> systemEmotion.price.asc();
            case "price_desc" -> systemEmotion.price.desc();
            case "name" -> systemEmotion.name.asc();
            case "latest" -> systemEmotion.createdAt.desc();
            default -> systemEmotion.sortOrder.asc();
        };
    }
}
