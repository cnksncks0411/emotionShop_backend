package com.app.emotion_market.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 포인트 계산 로직 유틸리티
 */
@Component
@Slf4j
public class PointCalculator {

    // 기본 포인트 상수
    private static final int BASE_EMOTION_SALE_POINTS = 20;
    private static final int DETAILED_STORY_BONUS = 5;
    private static final int REUSE_PERMISSION_BONUS = 5;
    private static final int DETAILED_STORY_MIN_LENGTH = 100;

    /**
     * 감정 판매 포인트 계산
     */
    public EmotionSalePoints calculateEmotionSalePoints(String story, boolean allowReuse) {
        int basePoints = BASE_EMOTION_SALE_POINTS;
        int bonusPoints = 0;
        
        EmotionSalePoints.BonusDetails bonusDetails = EmotionSalePoints.BonusDetails.builder()
                .detailedStoryBonus(0)
                .reusePermissionBonus(0)
                .build();

        // 상세 스토리 보너스 (100자 이상)
        if (story != null && story.trim().length() >= DETAILED_STORY_MIN_LENGTH) {
            bonusPoints += DETAILED_STORY_BONUS;
            bonusDetails.setDetailedStoryBonus(DETAILED_STORY_BONUS);
            log.debug("상세 스토리 보너스 적용: +{}EP (글자수: {})", DETAILED_STORY_BONUS, story.length());
        }

        // 재사용 허용 보너스
        if (allowReuse) {
            bonusPoints += REUSE_PERMISSION_BONUS;
            bonusDetails.setReusePermissionBonus(REUSE_PERMISSION_BONUS);
            log.debug("재사용 허용 보너스 적용: +{}EP", REUSE_PERMISSION_BONUS);
        }

        int totalPoints = basePoints + bonusPoints;

        log.info("감정 판매 포인트 계산 완료: 기본{}EP + 보너스{}EP = 총{}EP", 
                basePoints, bonusPoints, totalPoints);

        return EmotionSalePoints.builder()
                .basePoints(basePoints)
                .bonusPoints(bonusPoints)
                .totalPoints(totalPoints)
                .bonusDetails(bonusDetails)
                .build();
    }

    /**
     * 이벤트 보너스 포인트 계산 (추후 확장용)
     */
    public int calculateEventBonus(String eventType, int basePoints) {
        return switch (eventType) {
            case "DOUBLE_POINTS" -> basePoints; // 2배 이벤트
            case "WEEKEND_BONUS" -> basePoints / 2; // 주말 50% 보너스
            case "FIRST_SALE" -> 10; // 첫 판매 보너스
            default -> 0;
        };
    }

    /**
     * 연속 활동 보너스 계산
     */
    public int calculateStreakBonus(int consecutiveDays) {
        if (consecutiveDays >= 7) {
            return 50; // 7일 연속 보너스
        } else if (consecutiveDays >= 3) {
            return 20; // 3일 연속 보너스
        }
        return 0;
    }

    /**
     * 감정 판매 포인트 결과 DTO
     */
    public static class EmotionSalePoints {
        private final int basePoints;
        private final int bonusPoints;
        private final int totalPoints;
        private final BonusDetails bonusDetails;

        public EmotionSalePoints(int basePoints, int bonusPoints, int totalPoints, BonusDetails bonusDetails) {
            this.basePoints = basePoints;
            this.bonusPoints = bonusPoints;
            this.totalPoints = totalPoints;
            this.bonusDetails = bonusDetails;
        }

        public static EmotionSalePointsBuilder builder() {
            return new EmotionSalePointsBuilder();
        }

        public int getBasePoints() { return basePoints; }
        public int getBonusPoints() { return bonusPoints; }
        public int getTotalPoints() { return totalPoints; }
        public BonusDetails getBonusDetails() { return bonusDetails; }

        public static class EmotionSalePointsBuilder {
            private int basePoints;
            private int bonusPoints;
            private int totalPoints;
            private BonusDetails bonusDetails;

            public EmotionSalePointsBuilder basePoints(int basePoints) {
                this.basePoints = basePoints;
                return this;
            }

            public EmotionSalePointsBuilder bonusPoints(int bonusPoints) {
                this.bonusPoints = bonusPoints;
                return this;
            }

            public EmotionSalePointsBuilder totalPoints(int totalPoints) {
                this.totalPoints = totalPoints;
                return this;
            }

            public EmotionSalePointsBuilder bonusDetails(BonusDetails bonusDetails) {
                this.bonusDetails = bonusDetails;
                return this;
            }

            public EmotionSalePoints build() {
                return new EmotionSalePoints(basePoints, bonusPoints, totalPoints, bonusDetails);
            }
        }

        /**
         * 보너스 상세 내역
         */
        public static class BonusDetails {
            private int detailedStoryBonus;
            private int reusePermissionBonus;

            public BonusDetails(int detailedStoryBonus, int reusePermissionBonus) {
                this.detailedStoryBonus = detailedStoryBonus;
                this.reusePermissionBonus = reusePermissionBonus;
            }

            public static BonusDetailsBuilder builder() {
                return new BonusDetailsBuilder();
            }

            public int getDetailedStoryBonus() { return detailedStoryBonus; }
            public int getReusePermissionBonus() { return reusePermissionBonus; }

            public void setDetailedStoryBonus(int detailedStoryBonus) {
                this.detailedStoryBonus = detailedStoryBonus;
            }

            public void setReusePermissionBonus(int reusePermissionBonus) {
                this.reusePermissionBonus = reusePermissionBonus;
            }

            public static class BonusDetailsBuilder {
                private int detailedStoryBonus;
                private int reusePermissionBonus;

                public BonusDetailsBuilder detailedStoryBonus(int detailedStoryBonus) {
                    this.detailedStoryBonus = detailedStoryBonus;
                    return this;
                }

                public BonusDetailsBuilder reusePermissionBonus(int reusePermissionBonus) {
                    this.reusePermissionBonus = reusePermissionBonus;
                    return this;
                }

                public BonusDetails build() {
                    return new BonusDetails(detailedStoryBonus, reusePermissionBonus);
                }
            }
        }
    }
}
