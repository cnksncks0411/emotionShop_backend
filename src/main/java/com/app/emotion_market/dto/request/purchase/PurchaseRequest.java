package com.app.emotion_market.dto.request.purchase;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 감정 구매 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "감정 구매 요청")
public class PurchaseRequest {

    @NotNull(message = "감정 상품 ID는 필수입니다")
    @Schema(description = "구매할 감정 상품 ID", example = "1", required = true)
    private Long emotionId;

    @Size(max = 200, message = "구매 메시지는 최대 200자까지 입력 가능합니다")
    @Schema(description = "구매 메시지", example = "스트레스가 심해서 기쁨이 필요해요")
    private String purchaseMessage;
}
