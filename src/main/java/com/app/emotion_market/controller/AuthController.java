package com.app.emotion_market.controller;

import com.app.emotion_market.dto.request.LoginRequest;
import com.app.emotion_market.dto.request.RefreshTokenRequest;
import com.app.emotion_market.dto.request.RegisterRequest;
import com.app.emotion_market.dto.response.LoginResponse;
import com.app.emotion_market.dto.response.UserResponse;
import com.app.emotion_market.security.jwt.TokenDto;
import com.app.emotion_market.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "인증 API", description = "회원가입, 로그인, 토큰 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다. 신규 가입 시 100EP가 자동 지급됩니다.")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = authService.register(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "회원가입이 완료되었습니다.");
        response.put("data", userResponse);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "로그인이 완료되었습니다.");
        response.put("data", loginResponse);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenDto tokenDto = authService.refresh(request.getRefreshToken());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "토큰이 갱신되었습니다.");
        response.put("data", tokenDto);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그아웃", description = "현재 세션을 종료합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        // JWT는 무상태이므로 클라이언트에서 토큰을 삭제하면 됨
        // 필요시 Redis에 블랙리스트 토큰을 저장할 수 있음
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "로그아웃이 완료되었습니다.");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
