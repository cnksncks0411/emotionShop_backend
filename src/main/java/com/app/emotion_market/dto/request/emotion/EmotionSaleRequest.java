package com.app.emotion_market.dto.request.emotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 감정 판매 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "감정 판매 요청")
public class EmotionSaleRequest {

    @NotBlank(message = "감정 유형은 필수입니다")
    @Schema(description = "감정 유형", example = "JOY", required = true)
    private String emotionType;

    @NotNull(message = "감정 강도는 필수입니다")
    @Min(value = 1, message = "감정 강도는 1 이상이어야 합니다")
    @Max(value = 10, message = "감정 강도는 10 이하여야 합니다")
    @Schema(description = "감정 강도 (1-10)", example = "8", required = true)
    private Integer intensity;

    @NotBlank(message = "감정 스토리는 필수입니다")
    @Size(min = 10, max = 500, message = "감정 스토리는 10자 이상 500자 이하여야 합니다")
    @Schema(description = "감정을 느낀 상황에 대한 스토리", 
            example = "오늘 오랜만에 친구들과 만나서 정말 즐거웠다. 맛있는 음식도 먹고...", 
            required = true)
    private String story;

    @Schema(description = "감정을 느낀 장소", example = "CAFE")
    private String location;

    @Schema(description = "감정과 관련된 태그들")
    @Size(max = 5, message = "태그는 최대 5개까지 입력 가능합니다")
    private List<@NotBlank @Size(max = 20) String> tags;

    @Valid
    @Schema(description = "재사용 권한 설정")
    private ReusePermission reusePermission;

    /**
     * 재사용 권한 설정
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "감정 데이터 재사용 권한")
    public static class ReusePermission {

        @Schema(description = "재판매 허용 여부", example = "true")
        private boolean allowResale;

        @Schema(description = "창작물 제작 허용 여부", example = "false")
        private boolean allowCreativeUse;
    }
}
