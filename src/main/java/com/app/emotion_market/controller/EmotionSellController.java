package com.app.emotion_market.controller;

import com.app.emotion_market.common.util.EmotionValidator;
import com.app.emotion_market.common.util.PointCalculator;
import com.app.emotion_market.dto.request.emotion.EmotionSaleRequest;
import com.app.emotion_market.dto.response.common.ApiResponse;
import com.app.emotion_market.dto.response.emotion.EmotionSaleResponse;
import com.emotionshop.entity_market.service.emotion.EmotionSellService;
import com.app.emotion_market.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 감정 판매 API 컨트롤러
 */
@RestController
@RequestMapping("/api/emotions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "감정 판매", description = "감정 판매 관련 API")
public class EmotionSellController {

    private final EmotionSellService emotionSellService;
    private final PointService pointService;
    private final EmotionValidator emotionValidator;
    private final PointCalculator pointCalculator;

    /**
     * 감정 판매
     */
    @PostMapping("/sell")
    @Operation(summary = "감정 판매", description = "사용자의 감정을 판매하여 포인트를 획득합니다")
    public ResponseEntity<ApiResponse<EmotionSaleResponse>> sellEmotion(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody EmotionSaleRequest request) {
        
        log.info("감정 판매 요청: userId={}, emotionType={}", userDetails.getUsername(), request.getEmotionType());

        try {
            Long userId = Long.parseLong(userDetails.getUsername());

            // 1. 입력 데이터 검증
            validateEmotionSaleRequest(request);

            // 2. 일일 판매 제한 체크
            if (!emotionSellService.canSellToday(userId)) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.failure("오늘 판매 가능한 횟수를 모두 사용했습니다 (5/5)")
                );
            }

            // 3. 포인트 계산
            PointCalculator.EmotionSalePoints pointsInfo = pointCalculator.calculateEmotionSalePoints(
                request.getStory(), 
                request.getReusePermission() != null && request.getReusePermission().isAllowResale()
            );

            // 4. 감정 판매 처리
            var userEmotion = emotionSellService.sellEmotion(userId, request, pointsInfo.getTotalPoints());

            // 5. 포인트 지급
            pointService.giveEmotionSalePoints(
                userId, 
                userEmotion.getId(), 
                request.getStory(),
                request.getReusePermission() != null && request.getReusePermission().isAllowResale()
            );

            // 6. 응답 생성
            Integer newBalance = pointService.getUserPoints(userId);
            int remainingSales = 5 - emotionSellService.getTodaySalesCount(userId);

            EmotionSaleResponse response = EmotionSaleResponse.builder()
                    .emotionId(userEmotion.getId())
                    .emotionType(userEmotion.getEmotionType())
                    .intensity(userEmotion.getIntensity())
                    .pointsEarned(pointsInfo.getTotalPoints())
                    .bonusDetails(EmotionSaleResponse.BonusDetails.builder()
                            .basePoints(pointsInfo.getBasePoints())
                            .detailedStoryBonus(pointsInfo.getBonusDetails().getDetailedStoryBonus())
                            .reusePermissionBonus(pointsInfo.getBonusDetails().getReusePermissionBonus())
                            .build())
                    .newPointBalance(newBalance)
                    .remainingSalesToday(remainingSales)
                    .createdAt(userEmotion.getCreatedAt())
                    .build();

            log.info("감정 판매 완료: userId={}, emotionId={}, points={}", 
                    userId, userEmotion.getId(), pointsInfo.getTotalPoints());

            return ResponseEntity.ok(ApiResponse.success("감정이 성공적으로 판매되었습니다", response));

        } catch (IllegalStateException e) {
            log.warn("감정 판매 실패 - 상태 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("감정 판매 실패 - 입력 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("감정 판매 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("감정 판매 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 오늘의 감정 판매 현황 조회
     */
    @GetMapping("/sell/status")
    @Operation(summary = "오늘의 판매 현황", description = "오늘 감정 판매 현황을 조회합니다")
    public ResponseEntity<ApiResponse<EmotionSaleResponse.TodayStatus>> getTodayStatus(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            
            int todaySales = emotionSellService.getTodaySalesCount(userId);
            int remainingSales = 5 - todaySales;
            Integer todayEarnings = emotionSellService.getTodayEarnings(userId);

            EmotionSaleResponse.TodayStatus status = EmotionSaleResponse.TodayStatus.builder()
                    .emotionsSoldToday(todaySales)
                    .remainingSales(Math.max(0, remainingSales))
                    .dailyLimit(5)
                    .pointsEarnedToday(todayEarnings != null ? todayEarnings : 0)
                    .canSellMore(remainingSales > 0)
                    .build();

            return ResponseEntity.ok(ApiResponse.success("오늘의 판매 현황을 조회했습니다", status));

        } catch (Exception e) {
            log.error("오늘의 판매 현황 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("현황 조회 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 감정 판매 가능 여부 체크
     */
    @GetMapping("/sell/check")
    @Operation(summary = "판매 가능 여부 체크", description = "현재 감정을 판매할 수 있는지 확인합니다")
    public ResponseEntity<ApiResponse<Boolean>> checkCanSell(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            boolean canSell = emotionSellService.canSellToday(userId);
            
            String message = canSell ? 
                "감정을 판매할 수 있습니다" : 
                "오늘 판매 가능한 횟수를 모두 사용했습니다";

            return ResponseEntity.ok(ApiResponse.success(message, canSell));

        } catch (Exception e) {
            log.error("판매 가능 여부 체크 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("체크 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 감정 판매 요청 검증
     */
    private void validateEmotionSaleRequest(EmotionSaleRequest request) {
        // 감정 유형 검증
        emotionValidator.validateEmotionType(request.getEmotionType()).throwIfInvalid();
        
        // 감정 강도 검증
        emotionValidator.validateIntensity(request.getIntensity()).throwIfInvalid();
        
        // 스토리 검증
        emotionValidator.validateEmotionStory(request.getStory()).throwIfInvalid();
        
        // 태그 검증
        emotionValidator.validateTags(request.getTags()).throwIfInvalid();
    }
}
