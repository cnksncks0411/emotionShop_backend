package com.app.emotion_market.dto.response.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 포인트 통계 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "포인트 통계 응답")
public class PointStatisticsResponse {

    @Schema(description = "통계 기간", example = "2024-01")
    private String period;

    @Schema(description = "총 획득 포인트", example = "200")
    private Integer totalEarned;

    @Schema(description = "총 사용 포인트", example = "120")
    private Integer totalSpent;

    @Schema(description = "순 증가 포인트", example = "80")
    private Integer netGain;

    @Schema(description = "총 거래 횟수", example = "15")
    private Integer transactionCount;

    @Schema(description = "일별 통계")
    private List<DailySummary> dailyStats;

    @Schema(description = "수입원별 분석")
    private Map<String, Integer> sourceBreakdown;

    @Schema(description = "지출 분석")
    private Map<String, Integer> spendingBreakdown;

    /**
     * 일별 요약
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "일별 포인트 요약")
    public static class DailySummary {

        @Schema(description = "날짜", example = "2024-01-15")
        private String date;

        @Schema(description = "획득 포인트", example = "25")
        private Integer earned;

        @Schema(description = "사용 포인트", example = "30")
        private Integer spent;

        @Schema(description = "순 증감", example = "-5")
        private Integer net;

        @Schema(description = "거래 횟수", example = "2")
        private Integer transactionCount;
    }
}
