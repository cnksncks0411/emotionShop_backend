package com.app.emotion_market.enums;

/**
 * 포인트 거래 유형
 */
public enum TransactionType {
    EARNED("획득"),
    SPENT("사용"),
    REFUNDED("환불"),
    ADJUSTED("조정"); // 관리자가 포인트 조정

    private final String koreanName;

    TransactionType(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
