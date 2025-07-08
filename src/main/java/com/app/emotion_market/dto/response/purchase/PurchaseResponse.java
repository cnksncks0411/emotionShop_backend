package com.app.emotion_market.dto.response.purchase;

import com.app.emotion_market.enumType.EmotionType;
import com.app.emotion_market.enumType.PurchaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 구매 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "구매 응답")
public class PurchaseResponse {

    @Schema(description = "구매 ID", example = "1")
    private Long purchaseId;

    @Schema(description = "감정 상품 ID", example = "1")
    private Long emotionId;

    @Schema(description = "감정 상품 이름", example = "기쁨")
    private String emotionName;

    @Schema(description = "감정 유형", example = "JOY")
    private EmotionType emotionType;

    @Schema(description = "구매 가격 (포인트)", example = "30")
    private Integer price;

    @Schema(description = "구매 후 포인트 잔액", example = "120")
    private Integer newPointBalance;

    @Schema(description = "구매 일시")
    private LocalDateTime purchasedAt;

    @Schema(description = "만료 일시")
    private LocalDateTime expiresAt;

    @Schema(description = "구매 상태", example = "ACTIVE")
    private PurchaseStatus status;

    @Schema(description = "콘텐츠 접근 횟수", example = "3")
    private Integer accessCount;

    @Schema(description = "마지막 접근 일시")
    private LocalDateTime lastAccessedAt;

    @Schema(description = "평점 (1-5)", example = "5")
    private Integer rating;

    @Schema(description = "리뷰 내용", example = "정말 기분이 좋아졌어요!")
    private String reviewComment;

    @Schema(description = "만료 여부", example = "false")
    private Boolean isExpired;
}
