package com.app.emotion_market.controller;

import com.app.emotion_market.dto.response.common.ApiResponse;
import com.app.emotion_market.dto.response.point.PointBalanceResponse;
import com.app.emotion_market.dto.response.point.PointTransactionResponse;
import com.app.emotion_market.entity.PointTransaction;
import com.app.emotion_market.enumType.RelatedType;
import com.app.emotion_market.entity.User;
import com.app.emotion_market.service.PointTransactionService;
import com.app.emotion_market.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 포인트 관리 API 컨트롤러
 */
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "포인트 관리", description = "포인트 관련 API")
public class PointController {

    private final PointTransactionService pointTransactionService;
    private final UserService userService;

    /**
     * 포인트 잔액 조회
     */
    @GetMapping("/balance")
    @Operation(summary = "포인트 잔액 조회", description = "현재 포인트 잔액과 요약 정보를 조회합니다")
    public ResponseEntity<ApiResponse<PointBalanceResponse>> getPointBalance(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            // 포인트 통계 조회
            Integer totalEarned = pointTransactionService.getTotalEarnedPoints(userId);
            Integer totalSpent = pointTransactionService.getTotalSpentPoints(userId);

            PointBalanceResponse response = PointBalanceResponse.builder()
                    .currentBalance(user.getPoints())
                    .totalEarned(totalEarned != null ? totalEarned : 0)
                    .totalSpent(Math.abs(totalSpent != null ? totalSpent : 0))
                    .pendingPoints(0) // MVP에서는 보류 포인트 없음
                    .build();

            return ResponseEntity.ok(ApiResponse.success("포인트 잔액을 조회했습니다", response));

        } catch (Exception e) {
            log.error("포인트 잔액 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("포인트 잔액 조회 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * 포인트 거래 내역 조회
     */
    @GetMapping("/transactions")
    @Operation(summary = "포인트 거래 내역", description = "포인트 거래 내역을 조회합니다")
    public ResponseEntity<ApiResponse<Page<PointTransactionResponse>>> getPointTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "거래 유형 필터", example = "EMOTION_SALE")
            @RequestParam(required = false) RelatedType type,
            @Parameter(description = "시작 날짜", example = "2024-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜", example = "2024-01-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            
            // 날짜 범위 설정
            LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            
            Page<PointTransaction> transactionsPage = pointTransactionService.findUserTransactions(
                userId, type, startDateTime, endDateTime, pageable
            );
            
            Page<PointTransactionResponse> response = transactionsPage.map(this::convertToTransactionResponse);

            return ResponseEntity.ok(ApiResponse.success("포인트 거래 내역을 조회했습니다", response));

        } catch (Exception e) {
            log.error("포인트 거래 내역 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.failure("포인트 거래 내역 조회 중 오류가 발생했습니다")
            );
        }
    }

    /**
     * PointTransaction을 PointTransactionResponse로 변환
     */
    private PointTransactionResponse convertToTransactionResponse(PointTransaction transaction) {
        return PointTransactionResponse.builder()
                .transactionId(transaction.getId())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .description(transaction.getDescription())
                .balanceAfter(transaction.getBalanceAfter())
                .relatedType(transaction.getRelatedType())
                .relatedId(transaction.getRelatedId())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
