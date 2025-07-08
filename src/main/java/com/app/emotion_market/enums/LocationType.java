package com.app.emotion_market.enums;

/**
 * 감정을 느낀 장소 유형
 */
public enum LocationType {
    HOME("집"),
    OFFICE("회사"),
    SCHOOL("학교"),
    CAFE("카페"),
    RESTAURANT("식당"),
    TRANSPORTATION("교통수단"),
    OUTDOORS("야외"),
    OTHER("기타");

    private final String koreanName;

    LocationType(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
