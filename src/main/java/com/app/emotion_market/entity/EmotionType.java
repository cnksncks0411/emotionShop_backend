package com.app.emotion_market.entity;

public enum EmotionType {
    JOY("기쁨", "😊"),
    SADNESS("슬픔", "😢"),
    ANGER("분노", "😠"),
    FEAR("두려움", "😰"),
    SURPRISE("놀람", "😲"),
    DISGUST("혐오", "🤢"),
    PEACE("평온", "😌"),
    LOVE("사랑", "🥰");

    private final String koreanName;
    private final String emoji;

    EmotionType(String koreanName, String emoji) {
        this.koreanName = koreanName;
        this.emoji = emoji;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public String getEmoji() {
        return emoji;
    }
}
