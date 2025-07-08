package com.app.emotion_market.controller;

import com.app.emotion_market.dto.request.purchase.PurchaseRequest;
import com.app.emotion_market.dto.request.purchase.ReviewRequest;
import com.app.emotion_market.dto.response.common.ApiResponse;
import com.app.emotion_market.dto.response.purchase.PurchaseResponse;
import com.app.emotion_market.dto.response.purchase.ContentResponse;
import com.app.emotion_market.entity.Purchase;
import com.app.emotion_market.service.PurchaseService;
import com.app.emotion_market.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 감정 구매 API 컨트롤러
 */
@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "감정 구매", description = "감정 구매 관련 API")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final UserService userService;

    /**
     * 감정 구매
     */
    @PostMapping
    @Operation(summary = "감정 구매", description = "감정 상품을 구매합니다")
    public ResponseEntity<ApiResponse<PurchaseResponse>> purchaseEmotion(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PurchaseRequest request) {
        
        log.info("감정 구매 요청: userId={}, emotionId={}", userDetails.getUsername(), request.getEmotionId());

        try {
            Long userId = Long.parseLong(userDetails.getUsername());

            // 구매 처리
            Purchase purchase = purchaseService.purchaseEmotion(
                userId, 
                request.getEmotionId(), 
                request.getPurchaseMessage()
            );

            // 사용자 최신 정보 조회
            var user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            // 응답 생성
            PurchaseResponse response = PurchaseResponse.builder()
                    .purchaseId(purchase.getId())
                    .emotionId(purchase.getEmotion().getId())
                    .emotionName(purchase.getEmotion().getName())
                    .emotionType(purchase.getEmotion().getEmotionType())
                    .price(purchase.getPointsSpent())
                    .newPointBalance(user.getPoints())
                    .purchasedAt(purchase.getCreatedAt())
                    .expiresAt(purchase.getExpiresAt())
                    .status(purchase.getStatus())
                    .build();

            log.info("감정 구매 완료: purchaseId={}, userId={}, emotionId={}, price={}", 
                    purchase.getId(), userId, request.getEmotionId(), purchase.getPointsSpent());

            return ResponseEntity.ok(ApiResponse.success("감정을 성공적으로 구매했습니다", response));

        } catch (IllegalStateException e) {
            log.warn("감정 구매 실패 - 상태 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("감정 구매 실패 - 입력 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("감정 구매 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("감정 구매 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 사용자 구매 내역 조회
     */
    @GetMapping("/my-purchases")
    @Operation(summary = "내 구매 내역", description = "사용자의 감정 구매 내역을 조회합니다")
    public ResponseEntity<ApiResponse<Page<PurchaseResponse>>> getMyPurchases(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Purchase> purchasesPage = purchaseService.findUserPurchases(userId, pageable);
            Page<PurchaseResponse> response = purchasesPage.map(this::convertToPurchaseResponse);

            return ResponseEntity.ok(ApiResponse.success("구매 내역을 조회했습니다", response));

        } catch (Exception e) {
            log.error("구매 내역 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("구매 내역 조회 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 활성 구매 내역 조회 (만료되지 않은 것들)
     */
    @GetMapping("/active")
    @Operation(summary = "활성 구매 내역", description = "만료되지 않은 구매 내역을 조회합니다")
    public ResponseEntity<ApiResponse<List<PurchaseResponse>>> getActivePurchases(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            
            List<Purchase> activePurchases = purchaseService.findUserActivePurchases(userId);
            List<PurchaseResponse> response = activePurchases.stream()
                    .map(this::convertToPurchaseResponse)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success("활성 구매 내역을 조회했습니다", response));

        } catch (Exception e) {
            log.error("활성 구매 내역 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("활성 구매 내역 조회 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 구매 상세 조회
     */
    @GetMapping("/{purchaseId}")
    @Operation(summary = "구매 상세 조회", description = "특정 구매의 상세 정보를 조회합니다")
    public ResponseEntity<ApiResponse<PurchaseResponse>> getPurchaseDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "구매 ID", example = "1")
            @PathVariable Long purchaseId) {
        
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            
            Optional<Purchase> purchaseOpt = purchaseService.findUserPurchase(userId, purchaseId);
            
            if (purchaseOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            PurchaseResponse response = convertToPurchaseResponse(purchaseOpt.get());

            return ResponseEntity.ok(ApiResponse.success("구매 상세 정보를 조회했습니다", response));

        } catch (Exception e) {
            log.error("구매 상세 조회 중 오류 발생: purchaseId={}", purchaseId, e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("구매 상세 조회 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 구매한 콘텐츠 조회
     */
    @GetMapping("/{purchaseId}/content")
    @Operation(summary = "구매 콘텐츠 조회", description = "구매한 감정의 콘텐츠를 조회합니다")
    public ResponseEntity<ApiResponse<ContentResponse>> getPurchasedContent(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "구매 ID", example = "1")
            @PathVariable Long purchaseId) {
        
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            
            // 콘텐츠 접근 권한 체크
            if (!purchaseService.canAccessContent(userId, purchaseId)) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.failure("콘텐츠 이용 기간이 만료되었거나 접근 권한이 없습니다")
                );
            }

            // 구매 정보 조회
            Optional<Purchase> purchaseOpt = purchaseService.findUserPurchase(userId, purchaseId);
            if (purchaseOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Purchase purchase = purchaseOpt.get();

            // 콘텐츠 접근 기록
            purchaseService.recordContentAccess(userId, purchaseId);

            // 콘텐츠 응답 생성 (실제로는 JSON 파싱 필요)
            ContentResponse response = ContentResponse.builder()
                    .purchaseId(purchase.getId())
                    .emotionName(purchase.getEmotion().getName())
                    .emotionType(purchase.getEmotion().getEmotionType())
                    .purchasedAt(purchase.getCreatedAt())
                    .expiresAt(purchase.getExpiresAt())
                    .accessCount(purchase.getAccessCount() + 1)
                    .build();

            return ResponseEntity.ok(ApiResponse.success("구매한 콘텐츠를 조회했습니다", response));

        } catch (IllegalStateException e) {
            log.warn("콘텐츠 접근 실패: purchaseId={}, reason={}", purchaseId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("구매 콘텐츠 조회 중 오류 발생: purchaseId={}", purchaseId, e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("콘텐츠 조회 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 구매 리뷰 작성
     */
    @PostMapping("/{purchaseId}/review")
    @Operation(summary = "구매 리뷰 작성", description = "구매한 감정에 대한 리뷰를 작성합니다")
    public ResponseEntity<ApiResponse<String>> writeReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "구매 ID", example = "1")
            @PathVariable Long purchaseId,
            @Valid @RequestBody ReviewRequest request) {
        
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            
            purchaseService.writeReview(
                userId, 
                purchaseId, 
                request.getRating(), 
                request.getComment()
            );

            log.info("구매 리뷰 작성 완료: purchaseId={}, userId={}, rating={}", 
                    purchaseId, userId, request.getRating());

            return ResponseEntity.ok(ApiResponse.success("리뷰가 작성되었습니다"));

        } catch (IllegalArgumentException e) {
            log.warn("리뷰 작성 실패: purchaseId={}, reason={}", purchaseId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("리뷰 작성 중 오류 발생: purchaseId={}", purchaseId, e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("리뷰 작성 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * Purchase를 PurchaseResponse로 변환
     */
    private PurchaseResponse convertToPurchaseResponse(Purchase purchase) {
        return PurchaseResponse.builder()
                .purchaseId(purchase.getId())
                .emotionId(purchase.getEmotion().getId())
                .emotionName(purchase.getEmotion().getName())
                .emotionType(purchase.getEmotion().getEmotionType())
                .price(purchase.getPointsSpent())
                .purchasedAt(purchase.getCreatedAt())
                .expiresAt(purchase.getExpiresAt())
                .status(purchase.getStatus())
                .accessCount(purchase.getAccessCount())
                .lastAccessedAt(purchase.getLastAccessedAt())
                .rating(purchase.getRating())
                .reviewComment(purchase.getReviewComment())
                .isExpired(purchase.getExpiresAt().isBefore(LocalDateTime.now()))
                .build();
    }
}
