package com.app.emotion_market.controller;

import com.app.emotion_market.dto.request.emotion.EmotionSaleRequest;
import com.app.emotion_market.dto.response.common.ApiResponse;
import com.app.emotion_market.dto.response.emotion.EmotionSaleResponse;
import com.app.emotion_market.enumType.EmotionType;
import com.app.emotion_market.enumType.LocationType;
import com.app.emotion_market.entity.User;
import com.app.emotion_market.entity.UserEmotion;
import com.app.emotion_market.service.EmotionSellService;
import com.app.emotion_market.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final UserService userService;

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

            // 1. 일일 판매 제한 체크
            if (!emotionSellService.canSellToday(userId)) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.failure("오늘 판매 가능한 횟수를 모두 사용했습니다 (5/5)")
                );
            }

            // 2. 감정 유형 변환
            EmotionType emotionType;
            try {
                emotionType = EmotionType.valueOf(request.getEmotionType().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.failure("올바르지 않은 감정 유형입니다")
                );
            }

            // 3. 위치 타입 변환 (선택사항)
            LocationType locationType = null;
            if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
                try {
                    locationType = LocationType.valueOf(request.getLocation().toUpperCase());
                } catch (IllegalArgumentException e) {
                    // 잘못된 위치 타입이면 null로 처리
                    locationType = null;
                }
            }

            // 4. 태그 배열 변환
            String[] tags = null;
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                tags = request.getTags().toArray(new String[0]);
            }

            // 5. 재사용 권한 확인
            boolean allowResale = request.getReusePermission() != null && 
                                request.getReusePermission().isAllowResale();
            boolean allowCreativeUse = request.getReusePermission() != null && 
                                     request.getReusePermission().isAllowCreativeUse();

            // 6. 감정 판매 처리 (기존 Service 메서드 사용)
            UserEmotion userEmotion = emotionSellService.sellEmotion(
                userId, 
                emotionType, 
                request.getIntensity(),
                request.getStory(),
                locationType,
                tags,
                allowResale,
                allowCreativeUse
            );

            // 7. 사용자 최신 정보 조회
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            // 8. 남은 판매 횟수 계산
            int remainingSales = emotionSellService.getRemainingDailySales(userId);

            // 9. 응답 생성
            EmotionSaleResponse response = EmotionSaleResponse.builder()
                    .emotionId(userEmotion.getId())
                    .emotionType(userEmotion.getEmotionType())
                    .intensity(userEmotion.getIntensity())
                    .pointsEarned(userEmotion.getPointsEarned())
                    .bonusDetails(parseBonusDetails(userEmotion.getBonusDetails()))
                    .newPointBalance(user.getPoints())
                    .remainingSalesToday(remainingSales)
                    .createdAt(userEmotion.getCreatedAt())
                    .build();

            log.info("감정 판매 완료: userId={}, emotionId={}, points={}", 
                    userId, userEmotion.getId(), userEmotion.getPointsEarned());

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
            
            // 기존 Service 메서드 사용
            int remainingSales = emotionSellService.getRemainingDailySales(userId);
            int todaySales = 5 - remainingSales;
            
            // 오늘 획득한 포인트는 별도 계산이 필요 (기존 Service에 없음)
            // 우선 0으로 처리하고 필요시 Service에 메서드 추가
            Integer todayEarnings = 0;

            EmotionSaleResponse.TodayStatus status = EmotionSaleResponse.TodayStatus.builder()
                    .emotionsSoldToday(todaySales)
                    .remainingSales(Math.max(0, remainingSales))
                    .dailyLimit(5)
                    .pointsEarnedToday(todayEarnings)
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
     * 사용자 감정 판매 내역 조회
     */
    @GetMapping("/my-sales")
    @Operation(summary = "내 감정 판매 내역", description = "사용자의 감정 판매 내역을 조회합니다")
    public ResponseEntity<ApiResponse<List<UserEmotion>>> getMyEmotionSales(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            List<UserEmotion> emotions = emotionSellService.getUserEmotionHistory(userId);
            
            return ResponseEntity.ok(ApiResponse.success("감정 판매 내역을 조회했습니다", emotions));

        } catch (Exception e) {
            log.error("감정 판매 내역 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("내역 조회 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 보너스 상세 JSON을 파싱하여 DTO로 변환
     */
    private EmotionSaleResponse.BonusDetails parseBonusDetails(String bonusDetailsJson) {
        // 간단한 JSON 파싱 (실제로는 Jackson 등을 사용하는 것이 좋음)
        try {
            // 기본값 설정
            int basePoints = 20;
            int detailedStoryBonus = 0;
            int reusePermissionBonus = 0;

            if (bonusDetailsJson != null && !bonusDetailsJson.trim().isEmpty()) {
                // 간단한 파싱 (JSON 라이브러리 사용 권장)
                if (bonusDetailsJson.contains("\"detailedStoryBonus\": 5")) {
                    detailedStoryBonus = 5;
                }
                if (bonusDetailsJson.contains("\"reusePermissionBonus\": 5")) {
                    reusePermissionBonus = 5;
                }
            }

            return EmotionSaleResponse.BonusDetails.builder()
                    .basePoints(basePoints)
                    .detailedStoryBonus(detailedStoryBonus)
                    .reusePermissionBonus(reusePermissionBonus)
                    .build();
        } catch (Exception e) {
            log.warn("보너스 상세 파싱 실패: {}", bonusDetailsJson, e);
            return EmotionSaleResponse.BonusDetails.builder()
                    .basePoints(20)
                    .detailedStoryBonus(0)
                    .reusePermissionBonus(0)
                    .build();
        }
    }
}
