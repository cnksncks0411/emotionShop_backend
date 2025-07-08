package com.app.emotion_market.dto.response.shop;

import com.app.emotion_market.enumType.EmotionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상점 감정 상품 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "상점 감정 상품 응답")
public class ShopItemResponse {

    @Schema(description = "감정 상품 ID", example = "1")
    private Long emotionId;

    @Schema(description = "감정 유형", example = "JOY")
    private EmotionType emotionType;

    @Schema(description = "감정 상품 이름", example = "기쁨")
    private String name;

    @Schema(description = "감정 상품 설명", example = "하루를 밝게 만들어줄 기쁨을 선사합니다")
    private String description;

    @Schema(description = "가격 (포인트)", example = "30")
    private Integer price;

    @Schema(description = "평균 평점", example = "4.8")
    private Double averageRating;

    @Schema(description = "총 구매 횟수", example = "1234")
    private Integer totalPurchases;

    @Schema(description = "감정 이모지", example = "😊")
    private String emoji;

    @Schema(description = "콘텐츠 미리보기")
    private ContentPreview contentPreview;

    /**
     * 콘텐츠 미리보기
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "콘텐츠 미리보기")
    public static class ContentPreview {

        @Schema(description = "음악 콘텐츠 설명", example = "신나는 플레이리스트 5곡")
        private String music;

        @Schema(description = "영상 콘텐츠 설명", example = "웃음 영상 모음 3분")
        private String video;

        @Schema(description = "텍스트 콘텐츠 설명", example = "긍정 에너지 명언 5개")
        private String text;

        @Schema(description = "실천 가이드 설명", example = "기쁨 유지 가이드 3단계")
        private String guide;
    }
}
