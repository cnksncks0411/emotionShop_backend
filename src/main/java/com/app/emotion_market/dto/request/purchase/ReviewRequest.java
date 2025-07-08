package com.app.emotion_market.dto.request.purchase;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구매 리뷰 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "구매 리뷰 요청")
public class ReviewRequest {

    @NotNull(message = "평점은 필수입니다")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다")
    @Schema(description = "평점 (1-5점)", example = "5", required = true)
    private Integer rating;

    @Size(max = 500, message = "리뷰 내용은 최대 500자까지 입력 가능합니다")
    @Schema(description = "리뷰 내용", example = "정말 기분이 좋아졌어요! 음악이 특히 마음에 들었습니다.")
    private String comment;
}
