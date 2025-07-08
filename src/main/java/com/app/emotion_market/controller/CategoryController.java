package com.app.emotion_market.controller;

import com.app.emotion_market.dto.response.common.ApiResponse;
import com.app.emotion_market.dto.response.shop.CategoryResponse;
import com.app.emotion_market.entity.EmotionCategory;
import com.app.emotion_market.service.EmotionCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 감정 카테고리 API 컨트롤러
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "감정 카테고리", description = "감정 카테고리 관련 API")
public class CategoryController {

    private final EmotionCategoryService emotionCategoryService;

    /**
     * 모든 활성 카테고리 조회
     */
    @GetMapping
    @Operation(summary = "카테고리 목록 조회", description = "모든 활성 카테고리를 조회합니다")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        
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
     * 카테고리별 이모지 반환
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
}
