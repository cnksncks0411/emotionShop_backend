package com.app.emotion_market.entity;

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
