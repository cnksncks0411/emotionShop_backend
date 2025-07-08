package com.app.emotion_market.dto.response.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "카테고리 응답")
public class CategoryResponse {

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "카테고리 이름", example = "활력충전")
    private String name;

    @Schema(description = "카테고리 설명", example = "에너지가 필요할 때")
    private String description;

    @Schema(description = "카테고리 이모지", example = "🌟")
    private String emoji;

    @Schema(description = "카테고리 내 감정 개수", example = "8")
    private Integer emotionCount;

    @Schema(description = "평균 가격", example = "30")
    private Integer averagePrice;

    @Schema(description = "총 구매 횟수", example = "5000")
    private Long totalPurchases;

    @Schema(description = "카테고리 색상 코드", example = "#FFD700")
    private String colorCode;
}
