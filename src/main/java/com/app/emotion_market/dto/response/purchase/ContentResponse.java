package com.app.emotion_market.dto.response.purchase;

import com.app.emotion_market.enumType.EmotionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 구매한 콘텐츠 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "구매한 콘텐츠 응답")
public class ContentResponse {

    @Schema(description = "구매 ID", example = "1")
    private Long purchaseId;

    @Schema(description = "감정 상품 이름", example = "기쁨")
    private String emotionName;

    @Schema(description = "감정 유형", example = "JOY")
    private EmotionType emotionType;

    @Schema(description = "구매 일시")
    private LocalDateTime purchasedAt;

    @Schema(description = "만료 일시")
    private LocalDateTime expiresAt;

    @Schema(description = "콘텐츠 접근 횟수", example = "3")
    private Integer accessCount;

    @Schema(description = "콘텐츠 정보")
    private ContentsData contents;

    /**
     * 콘텐츠 데이터
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "콘텐츠 데이터")
    public static class ContentsData {

        @Schema(description = "음악 플레이리스트")
        private List<MusicItem> music;

        @Schema(description = "영상 목록")
        private List<VideoItem> videos;

        @Schema(description = "텍스트 콘텐츠")
        private List<TextItem> texts;

        @Schema(description = "실천 가이드")
        private List<GuideItem> guides;
    }

    /**
     * 음악 아이템
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "음악 아이템")
    public static class MusicItem {

        @Schema(description = "제목", example = "Happy")
        private String title;

        @Schema(description = "아티스트", example = "Pharrell Williams")
        private String artist;

        @Schema(description = "재생 시간", example = "3:53")
        private String duration;

        @Schema(description = "Spotify URL", example = "https://open.spotify.com/track/...")
        private String spotifyUrl;

        @Schema(description = "YouTube URL", example = "https://www.youtube.com/watch?v=...")
        private String youtubeUrl;
    }

    /**
     * 영상 아이템
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "영상 아이템")
    public static class VideoItem {

        @Schema(description = "제목", example = "3분간의 웃음 영상")
        private String title;

        @Schema(description = "설명", example = "스트레스가 날아가는 재미있는 영상")
        private String description;

        @Schema(description = "썸네일 URL", example = "https://example.com/thumb1.jpg")
        private String thumbnail;

        @Schema(description = "영상 URL", example = "https://www.youtube.com/watch?v=...")
        private String url;

        @Schema(description = "재생 시간", example = "3:15")
        private String duration;
    }

    /**
     * 텍스트 아이템
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "텍스트 아이템")
    public static class TextItem {

        @Schema(description = "유형", example = "QUOTE")
        private String type;

        @Schema(description = "제목", example = "오늘의 긍정 명언")
        private String title;

        @Schema(description = "내용", example = "행복은 준비된 마음에 찾아오는 나비와 같다.")
        private String content;

        @Schema(description = "작가", example = "너새니얼 호손")
        private String author;
    }

    /**
     * 가이드 아이템
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "가이드 아이템")
    public static class GuideItem {

        @Schema(description = "제목", example = "기쁨을 오래 유지하는 방법")
        private String title;

        @Schema(description = "실천 단계")
        private List<GuideStep> steps;
    }

    /**
     * 가이드 단계
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "가이드 단계")
    public static class GuideStep {

        @Schema(description = "단계 번호", example = "1")
        private Integer step;

        @Schema(description = "단계 제목", example = "감사 일기 쓰기")
        private String title;

        @Schema(description = "단계 설명", example = "오늘 감사했던 3가지를 적어보세요.")
        private String description;
    }
}
