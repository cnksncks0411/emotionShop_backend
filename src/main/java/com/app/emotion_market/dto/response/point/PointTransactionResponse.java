package com.app.emotion_market.dto.response.point;

import com.app.emotion_market.enumType.RelatedType;
import com.app.emotion_market.enumType.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 포인트 거래 내역 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "포인트 거래 내역 응답")
public class PointTransactionResponse {

    @Schema(description = "거래 ID", example = "789")
    private Long transactionId;

    @Schema(description = "거래 포인트 (양수: 획득, 음수: 사용)", example = "25")
    private Integer amount;

    @Schema(description = "거래 유형", example = "EMOTION_SALE")
    private TransactionType transactionType;

    @Schema(description = "거래 설명", example = "감정 판매 (기쁨)")
    private String description;

    @Schema(description = "거래 후 잔액", example = "150")
    private Integer balanceAfter;

    @Schema(description = "관련 레코드 타입", example = "EMOTION_SALE")
    private RelatedType relatedType;

    @Schema(description = "관련 레코드 ID", example = "123")
    private Long relatedId;

    @Schema(description = "거래 일시")
    private LocalDateTime createdAt;
}
