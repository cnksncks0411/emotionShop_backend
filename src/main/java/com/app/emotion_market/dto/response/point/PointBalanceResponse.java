package com.app.emotion_market.dto.response.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 포인트 잔액 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "포인트 잔액 응답")
public class PointBalanceResponse {

    @Schema(description = "현재 포인트 잔액", example = "150")
    private Integer currentBalance;

    @Schema(description = "총 획득 포인트", example = "300")
    private Integer totalEarned;

    @Schema(description = "총 사용 포인트", example = "150")
    private Integer totalSpent;

    @Schema(description = "보류 중인 포인트", example = "0")
    private Integer pendingPoints;

    @Schema(description = "만료 예정 포인트 정보")
    private ExpiringPoints expiringPoints;

    /**
     * 만료 예정 포인트 정보
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "만료 예정 포인트")
    public static class ExpiringPoints {

        @Schema(description = "만료 예정 포인트 양", example = "20")
        private Integer amount;

        @Schema(description = "만료 예정일", example = "2024-02-15")
        private String expiryDate;
    }
}
