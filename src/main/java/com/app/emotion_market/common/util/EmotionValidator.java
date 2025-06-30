package com.app.emotion_market.common.util;

import com.emotionshop.entity_market.common.enums.EmotionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 감정 데이터 검증 유틸리티
 */
@Component
@Slf4j
public class EmotionValidator {

    // 금지어 목록 (실제로는 더 포괄적인 리스트 필요)
    private static final Set<String> FORBIDDEN_WORDS = Set.of(
            "씨발", "개새끼", "병신", "미친", "죽어", "죽일", "살인", "자살",
            "fuck", "shit", "damn", "bitch", "asshole"
    );

    // 스팸 패턴
    private static final Pattern SPAM_PATTERN = Pattern.compile(
            ".*(광고|홍보|판매|구매|돈|대출|투자|수익|이벤트|혜택|할인|무료|공짜|click|링크|URL|http|www).*",
            Pattern.CASE_INSENSITIVE
    );

    // 반복 문자 패턴 (3개 이상 연속)
    private static final Pattern REPEAT_PATTERN = Pattern.compile("(.)\\1{2,}");

    /**
     * 감정 스토리 종합 검증
     */
    public ValidationResult validateEmotionStory(String story) {
        if (story == null || story.trim().isEmpty()) {
            return ValidationResult.failure("감정 스토리를 입력해주세요");
        }

        String trimmedStory = story.trim();

        // 길이 검증
        if (trimmedStory.length() < 10) {
            return ValidationResult.failure("감정 스토리는 최소 10자 이상 입력해주세요");
        }

        if (trimmedStory.length() > 500) {
            return ValidationResult.failure("감정 스토리는 최대 500자까지 입력 가능합니다");
        }

        // 금지어 검증
        if (containsForbiddenWords(trimmedStory)) {
            return ValidationResult.failure("부적절한 언어가 포함되어 있습니다");
        }

        // 스팸 검증
        if (isSpamContent(trimmedStory)) {
            return ValidationResult.failure("광고성 내용은 입력할 수 없습니다");
        }

        // 반복 문자 검증
        if (hasExcessiveRepeatedChars(trimmedStory)) {
            return ValidationResult.failure("동일한 문자를 과도하게 반복할 수 없습니다");
        }

        // 의미있는 내용 검증
        if (!hasMeaningfulContent(trimmedStory)) {
            return ValidationResult.failure("의미있는 감정 스토리를 작성해주세요");
        }

        return ValidationResult.success();
    }

    /**
     * 감정 유형 검증
     */
    public ValidationResult validateEmotionType(String emotionType) {
        if (emotionType == null || emotionType.trim().isEmpty()) {
            return ValidationResult.failure("감정 유형을 선택해주세요");
        }

        try {
            EmotionType.valueOf(emotionType.trim().toUpperCase());
            return ValidationResult.success();
        } catch (IllegalArgumentException e) {
            return ValidationResult.failure("올바르지 않은 감정 유형입니다");
        }
    }

    /**
     * 감정 강도 검증
     */
    public ValidationResult validateIntensity(Integer intensity) {
        if (intensity == null) {
            return ValidationResult.failure("감정 강도를 선택해주세요");
        }

        if (intensity < 1 || intensity > 10) {
            return ValidationResult.failure("감정 강도는 1~10 사이의 값이어야 합니다");
        }

        return ValidationResult.success();
    }

    /**
     * 태그 검증
     */
    public ValidationResult validateTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return ValidationResult.success(); // 태그는 선택사항
        }

        if (tags.size() > 5) {
            return ValidationResult.failure("태그는 최대 5개까지 입력 가능합니다");
        }

        for (String tag : tags) {
            if (tag == null || tag.trim().isEmpty()) {
                return ValidationResult.failure("빈 태그는 입력할 수 없습니다");
            }

            if (tag.trim().length() > 20) {
                return ValidationResult.failure("태그는 최대 20자까지 입력 가능합니다");
            }

            if (containsForbiddenWords(tag.trim())) {
                return ValidationResult.failure("태그에 부적절한 언어가 포함되어 있습니다");
            }
        }

        return ValidationResult.success();
    }

    /**
     * 금지어 포함 여부 체크
     */
    private boolean containsForbiddenWords(String text) {
        String lowerText = text.toLowerCase();
        return FORBIDDEN_WORDS.stream().anyMatch(lowerText::contains);
    }

    /**
     * 스팸 내용 여부 체크
     */
    private boolean isSpamContent(String text) {
        return SPAM_PATTERN.matcher(text).matches();
    }

    /**
     * 과도한 반복 문자 여부 체크
     */
    private boolean hasExcessiveRepeatedChars(String text) {
        return REPEAT_PATTERN.matcher(text).find();
    }

    /**
     * 의미있는 내용 여부 체크
     */
    private boolean hasMeaningfulContent(String text) {
        // 공백 제거 후 실제 문자 수 체크
        String meaningfulText = text.replaceAll("\\s+", "");
        if (meaningfulText.length() < 5) {
            return false;
        }

        // 단순 반복이나 무의미한 패턴 체크
        if (meaningfulText.matches("^(.{1,3})\\1+$")) {
            return false; // "ㅋㅋㅋㅋㅋ", "하하하하" 등
        }

        // 숫자나 특수문자만으로 구성된 내용 체크
        if (meaningfulText.matches("^[\\d\\p{Punct}\\s]+$")) {
            return false;
        }

        return true;
    }

    /**
     * 일일 판매 제한 체크를 위한 도우미 메서드
     */
    public boolean isWithinDailyLimit(int currentSalesCount) {
        return currentSalesCount < 5; // 일일 최대 5건
    }

    /**
     * 구매 메시지 검증
     */
    public ValidationResult validatePurchaseMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return ValidationResult.success(); // 구매 메시지는 선택사항
        }

        String trimmedMessage = message.trim();

        if (trimmedMessage.length() > 200) {
            return ValidationResult.failure("구매 메시지는 최대 200자까지 입력 가능합니다");
        }

        if (containsForbiddenWords(trimmedMessage)) {
            return ValidationResult.failure("구매 메시지에 부적절한 언어가 포함되어 있습니다");
        }

        return ValidationResult.success();
    }

    /**
     * 검증 결과 클래스
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void throwIfInvalid() {
            if (!valid) {
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }
}
