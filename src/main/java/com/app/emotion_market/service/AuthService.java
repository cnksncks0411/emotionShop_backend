package com.app.emotion_market.service;

import com.app.emotion_market.common.exception.DuplicateEmailException;
import com.app.emotion_market.common.exception.UserNotFoundException;
import com.app.emotion_market.dto.request.LoginRequest;
import com.app.emotion_market.dto.request.RegisterRequest;
import com.app.emotion_market.dto.response.LoginResponse;
import com.app.emotion_market.dto.response.UserResponse;
import com.app.emotion_market.entity.User;
import com.app.emotion_market.repository.UserRepository;
import com.app.emotion_market.security.jwt.JwtTokenProvider;
import com.app.emotion_market.security.jwt.TokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PointService pointService;

    /**
     * 회원가입
     */
    public UserResponse register(RegisterRequest request) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 가입된 이메일입니다.");
        }

        // 닉네임 중복 검사
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateEmailException("이미 사용 중인 닉네임입니다.");
        }

        // 필수 약관 동의 검사
        if (!request.getAgreeTerms() || !request.getAgreePrivacy()) {
            throw new IllegalArgumentException("필수 약관에 동의해야 합니다.");
        }

        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .points(100) // 신규 가입 보너스 100EP
                .agreeTerms(request.getAgreeTerms())
                .agreePrivacy(request.getAgreePrivacy())
                .agreeMarketing(request.getAgreeMarketing())
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(user);

        // 신규 가입 보너스 포인트 거래 내역 생성
        pointService.addPoints(savedUser.getId(), 100, "SIGNUP_BONUS", "신규 가입 보너스", null, null);

        log.info("신규 사용자 가입 완료: {}", savedUser.getEmail());

        return UserResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .points(savedUser.getPoints())
                .mbtiType(savedUser.getMbtiType())
                .profileImage(savedUser.getProfileImage())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    /**
     * 로그인
     */
    public LoginResponse login(LoginRequest request) {
        // 사용자 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 토큰 생성
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        // 사용자 정보 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 마지막 로그인 시간 업데이트
        user.updateLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("사용자 로그인 완료: {}", user.getEmail());

        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .points(user.getPoints())
                .accessToken(tokenDto.getAccessToken())
                .refreshToken(tokenDto.getRefreshToken())
                .tokenType(tokenDto.getGrantType())
                .expiresIn(tokenDto.getAccessTokenExpiresIn())
                .build();
    }

    /**
     * 토큰 갱신
     */
    public TokenDto refresh(String refreshToken) {
        // Refresh Token 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh Token이 유효하지 않습니다.");
        }

        // 현재 인증 정보로 새로운 토큰 생성
        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        log.info("토큰 갱신 완료: {}", authentication.getName());

        return tokenDto;
    }
}
