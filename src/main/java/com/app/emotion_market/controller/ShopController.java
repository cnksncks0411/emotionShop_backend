package com.app.emotion_market.controller;

import com.app.emotion_market.dto.response.common.ApiResponse;
import com.app.emotion_market.dto.response.shop.ShopItemResponse;
import com.app.emotion_market.dto.response.shop.CategoryResponse;
import com.app.emotion_market.enumType.EmotionType;
import com.app.emotion_market.entity.SystemEmotion;
import com.app.emotion_market.service.SystemEmotionService;
import com.app.emotion_market.service.EmotionCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 감정 상점 API 컨트롤러
 */
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "감정 상점", description = "감정 상점 관련 API")
public class ShopController {

    private final SystemEmotionService systemEmotionService;
    private final EmotionCategoryService emotionCategoryService;

    /**
     * 상점 메인 화면 - 카테고리 목록 조회
     */
    @GetMapping("/categories")
    @Operation(summary = "카테고리 목록 조회", description = "모든 감정 카테고리를 조회합니다")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        try {
            var categoriesWithStats = emotionCategoryService.findCategoriesWithStats();
            
            List<CategoryResponse> response = categoriesWithStats.stream()
                    .map(categoryStats -> CategoryResponse.builder()
                            .categoryId(categoryStats.getCategory().getId())
                            .name(categoryStats.getCategory().getName())
                            .description(categoryStats.getCategory().getDescription())
                            .emoji(getEmojiForCategory(categoryStats.getCategory().getName()))
                            .emotionCount(categoryStats.getEmotionCount().intValue())
                            .averagePrice(categoryStats.getAveragePrice().intValue())
                            .totalPurchases(categoryStats.getTotalPurchases())
                            .colorCode(categoryStats.getCategory().getColorCode())
                            .build())
                    .toList();

            return ResponseEntity.ok(ApiResponse.success("카테고리 목록을 조회했습니다", response));

        } catch (Exception e) {
            log.error("카테고리 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("카테고리 조회 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 오늘의 베스트 감정 조회
     */
    @GetMapping("/best")
    @Operation(summary = "오늘의 베스트 감정", description = "오늘의 인기 감정 상품을 조회합니다")
    public ResponseEntity<ApiResponse<List<ShopItemResponse>>> getTodaysBest(
            @Parameter(description = "조회할 개수", example = "4")
            @RequestParam(defaultValue = "4") int limit) {
        
        try {
            List<SystemEmotion> bestEmotions = systemEmotionService.findTodaysBestEmotions(limit);
            
            List<ShopItemResponse> response = bestEmotions.stream()
                    .map(this::convertToShopItemResponse)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success("오늘의 베스트 감정을 조회했습니다", response));

        } catch (Exception e) {
            log.error("베스트 감정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("베스트 감정 조회 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 카테고리별 감정 상품 목록 조회
     */
    @GetMapping("/categories/{categoryId}/emotions")
    @Operation(summary = "카테고리별 감정 목록", description = "특정 카테고리의 감정 상품들을 조회합니다")
    public ResponseEntity<ApiResponse<Page<ShopItemResponse>>> getEmotionsByCategory(
            @Parameter(description = "카테고리 ID", example = "1")
            @PathVariable Long categoryId,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "12")
            @RequestParam(defaultValue = "12") int size,
            @Parameter(description = "정렬 기준", example = "popularity")
            @RequestParam(defaultValue = "popularity") String sort) {
        
        try {
            Sort sortOrder = getSortOrder(sort);
            Pageable pageable = PageRequest.of(page, size, sortOrder);
            
            Page<SystemEmotion> emotionsPage = systemEmotionService.findEmotionsByCategory(categoryId, pageable);
            
            Page<ShopItemResponse> response = emotionsPage.map(this::convertToShopItemResponse);

            return ResponseEntity.ok(ApiResponse.success("카테고리별 감정 목록을 조회했습니다", response));

        } catch (Exception e) {
            log.error("카테고리별 감정 조회 중 오류 발생: categoryId={}", categoryId, e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("감정 목록 조회 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 감정 상품 상세 조회
     */
    @GetMapping("/emotions/{emotionId}")
    @Operation(summary = "감정 상품 상세", description = "특정 감정 상품의 상세 정보를 조회합니다")
    public ResponseEntity<ApiResponse<ShopItemResponse>> getEmotionDetail(
            @Parameter(description = "감정 상품 ID", example = "1")
            @PathVariable Long emotionId) {
        
        try {
            Optional<SystemEmotion> emotionOpt = systemEmotionService.findEmotionById(emotionId);
            
            if (emotionOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ShopItemResponse response = convertToShopItemResponse(emotionOpt.get());

            return ResponseEntity.ok(ApiResponse.success("감정 상품 상세 정보를 조회했습니다", response));

        } catch (Exception e) {
            log.error("감정 상세 조회 중 오류 발생: emotionId={}", emotionId, e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("감정 상세 조회 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 감정 검색
     */
    @GetMapping("/search")
    @Operation(summary = "감정 검색", description = "키워드로 감정 상품을 검색합니다")
    public ResponseEntity<ApiResponse<List<ShopItemResponse>>> searchEmotions(
            @Parameter(description = "검색 키워드", example = "기쁨")
            @RequestParam String keyword,
            @Parameter(description = "최소 가격", example = "20")
            @RequestParam(required = false) Integer minPrice,
            @Parameter(description = "최대 가격", example = "50")
            @RequestParam(required = false) Integer maxPrice,
            @Parameter(description = "최소 평점", example = "4.0")
            @RequestParam(required = false) Double minRating) {
        
        try {
            List<SystemEmotion> emotions;

            // 키워드 검색
            emotions = systemEmotionService.searchEmotionsByName(keyword);

            // 가격 필터링
            if (minPrice != null || maxPrice != null) {
                emotions = emotions.stream()
                        .filter(emotion -> {
                            int price = emotion.getPrice();
                            boolean priceMatch = true;
                            if (minPrice != null) priceMatch &= price >= minPrice;
                            if (maxPrice != null) priceMatch &= price <= maxPrice;
                            return priceMatch;
                        })
                        .toList();
            }

            // 평점 필터링
            if (minRating != null) {
                emotions = emotions.stream()
                        .filter(emotion -> emotion.getAverageRating() >= minRating)
                        .toList();
            }

            List<ShopItemResponse> response = emotions.stream()
                    .map(this::convertToShopItemResponse)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(
                String.format("'%s' 검색 결과 %d건을 조회했습니다", keyword, response.size()), 
                response
            ));

        } catch (Exception e) {
            log.error("감정 검색 중 오류 발생: keyword={}", keyword, e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("검색 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 인기 감정 TOP N 조회
     */
    @GetMapping("/popular")
    @Operation(summary = "인기 감정 TOP N", description = "인기 감정 상품 순위를 조회합니다")
    public ResponseEntity<ApiResponse<List<ShopItemResponse>>> getPopularEmotions(
            @Parameter(description = "조회할 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<SystemEmotion> popularEmotions = systemEmotionService.findPopularEmotions(limit);
            
            List<ShopItemResponse> response = popularEmotions.stream()
                    .map(this::convertToShopItemResponse)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success("인기 감정 순위를 조회했습니다", response));

        } catch (Exception e) {
            log.error("인기 감정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("인기 감정 조회 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 추천 감정 조회 (평점 기준)
     */
    @GetMapping("/recommended")
    @Operation(summary = "추천 감정", description = "평점 기준 추천 감정을 조회합니다")
    public ResponseEntity<ApiResponse<List<ShopItemResponse>>> getRecommendedEmotions(
            @Parameter(description = "조회할 개수", example = "4")
            @RequestParam(defaultValue = "4") int limit) {
        
        try {
            List<SystemEmotion> recommendedEmotions = systemEmotionService.findRecommendedEmotions(limit);
            
            List<ShopItemResponse> response = recommendedEmotions.stream()
                    .map(this::convertToShopItemResponse)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success("추천 감정을 조회했습니다", response));

        } catch (Exception e) {
            log.error("추천 감정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("추천 감정 조회 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * SystemEmotion을 ShopItemResponse로 변환
     */
    private ShopItemResponse convertToShopItemResponse(SystemEmotion emotion) {
        return ShopItemResponse.builder()
                .emotionId(emotion.getId())
                .emotionType(emotion.getEmotionType())
                .name(emotion.getName())
                .description(emotion.getDescription())
                .price(emotion.getPrice())
                .averageRating(emotion.getAverageRating())
                .totalPurchases(emotion.getTotalPurchases())
                .emoji(getEmojiForEmotionType(emotion.getEmotionType()))
                .contentPreview(parseContentPreview(emotion.getContents()))
                .build();
    }

    /**
     * 정렬 기준 변환
     */
    private Sort getSortOrder(String sort) {
        return switch (sort.toLowerCase()) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "rating" -> Sort.by("averageRating").descending();
            case "name" -> Sort.by("name").ascending();
            case "popularity" -> Sort.by("totalPurchases").descending();
            default -> Sort.by("totalPurchases").descending(); // 기본값: 인기순
        };
    }

    /**
     * 카테고리 이름에 따른 이모지 반환
     */
    private String getEmojiForCategory(String categoryName) {
        return switch (categoryName) {
            case "활력충전" -> "🌟";
            case "마음따뜻" -> "💝";
            case "에너지폭발" -> "🔥";
            case "마음정리" -> "🌿";
            case "속시원" -> "😤";
            case "감정해소" -> "😢";
            case "긴장감체험" -> "😰";
            case "깊은사색" -> "🤔";
            default -> "🎭";
        };
    }

    /**
     * 감정 유형에 따른 이모지 반환
     */
    private String getEmojiForEmotionType(EmotionType emotionType) {
        return switch (emotionType) {
            case JOY -> "😊";
            case SADNESS -> "😢";
            case ANGER -> "😠";
            case FEAR -> "😰";
            case SURPRISE -> "😲";
            case DISGUST -> "🤢";
            case PEACE -> "😌";
            case LOVE -> "🥰";
            default -> "🎭";
        };
    }

    /**
     * 콘텐츠 미리보기 파싱
     */
    private ShopItemResponse.ContentPreview parseContentPreview(String contentsJson) {
        // 간단한 JSON 파싱 (실제로는 Jackson 등을 사용하는 것이 좋음)
        try {
            return ShopItemResponse.ContentPreview.builder()
                    .music("신나는 플레이리스트 5곡")
                    .video("감정 체험 영상 3분")
                    .text("긍정 에너지 명언 5개")
                    .guide("실천 가이드 3단계")
                    .build();
        } catch (Exception e) {
            log.warn("콘텐츠 미리보기 파싱 실패: {}", contentsJson, e);
            return ShopItemResponse.ContentPreview.builder()
                    .music("음악 콘텐츠")
                    .video("영상 콘텐츠")
                    .text("텍스트 콘텐츠")
                    .guide("실천 가이드")
                    .build();
        }
    }
}
