package com.app.emotion_market.entity;

public enum RelatedType {
    EMOTION_SALE("감정 판매"),
    EMOTION_PURCHASE("감정 구매"),
    SIGNUP_BONUS("가입 보너스"),
    EVENT("이벤트"),
    ADMIN_ADJUSTMENT("관리자 조정"),
    REFUND("환불");

    private final String koreanName;

    RelatedType(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
