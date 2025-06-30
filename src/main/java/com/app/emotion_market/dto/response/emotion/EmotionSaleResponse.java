package com.app.emotion_market.dto.response.emotion;

import com.emotionshop.entity_market.common.enums.EmotionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 감정 판매 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "감정 판매 응답")
public class EmotionSaleResponse {

    @Schema(description = "판매된 감정 ID", example = "123")
    private Long emotionId;

    @Schema(description = "감정 유형", example = "JOY")
    private EmotionType emotionType;

    @Schema(description = "감정 강도", example = "8")
    private Integer intensity;

    @Schema(description = "획득한 포인트", example = "25")
    private Integer pointsEarned;

    @Schema(description = "포인트 획득 상세 내역")
    private BonusDetails bonusDetails;

    @Schema(description = "포인트 적용 후 새로운 잔액", example = "175")
    private Integer newPointBalance;

    @Schema(description = "오늘 남은 판매 횟수", example = "2")
    private Integer remainingSalesToday;

    @Schema(description = "판매 일시")
    private LocalDateTime createdAt;

    /**
     * 포인트 보너스 상세 내역
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "포인트 보너스 상세 내역")
    public static class BonusDetails {

        @Schema(description = "기본 포인트", example = "20")
        private Integer basePoints;

        @Schema(description = "상세 스토리 보너스", example = "5")
        private Integer detailedStoryBonus;

        @Schema(description = "재사용 허용 보너스", example = "0")
        private Integer reusePermissionBonus;
    }

    /**
     * 오늘의 판매 현황
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "오늘의 감정 판매 현황")
    public static class TodayStatus {

        @Schema(description = "오늘 판매한 감정 수", example = "3")
        private Integer emotionsSoldToday;

        @Schema(description = "남은 판매 횟수", example = "2")
        private Integer remainingSales;

        @Schema(description = "일일 판매 제한", example = "5")
        private Integer dailyLimit;

        @Schema(description = "오늘 획득한 총 포인트", example = "75")
        private Integer pointsEarnedToday;

        @Schema(description = "추가 판매 가능 여부", example = "true")
        private Boolean canSellMore;
    }
}
