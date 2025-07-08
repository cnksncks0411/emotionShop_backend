package com.app.emotion_market.service;

import com.app.emotion_market.entity.User;
import com.app.emotion_market.enumType.UserStatus;
import com.app.emotion_market.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PointTransactionService pointTransactionService;

    @Transactional
    public User createUser(String email, String password, String nickname, 
                          Boolean agreeTerms, Boolean agreePrivacy, Boolean agreeMarketing) {
        // 중복 체크
        validateDuplicateUser(email, nickname);

        // 사용자 생성
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .agreeTerms(agreeTerms)
                .agreePrivacy(agreePrivacy)
                .agreeMarketing(agreeMarketing != null ? agreeMarketing : false)
                .build();

        User savedUser = userRepository.save(user);

        // 가입 보너스 포인트 트랜잭션 기록
        pointTransactionService.recordSignupBonus(savedUser);

        log.info("새 사용자 가입: email={}, nickname={}", email, nickname);
        return savedUser;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findActiveUserByEmail(String email) {
        return userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE);
    }

    @Transactional
    public void updateLastLogin(Long userId) {
        userRepository.findById(userId)
                .ifPresent(User::updateLastLogin);
    }

    @Transactional
    public User updateProfile(Long userId, String nickname, String profileImage, String mbtiType) {
        User user = getUserById(userId);
        
        // 닉네임 중복 체크 (본인 제외)
        if (nickname != null && !nickname.equals(user.getNickname())) {
            if (userRepository.existsByNickname(nickname)) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다");
            }
        }

        user.updateProfile(nickname, profileImage, mbtiType);
        return user;
    }

    @Transactional
    public void addPoints(Long userId, Integer points) {
        User user = getUserById(userId);
        user.addPoints(points);
        log.debug("사용자 {} 포인트 {} 추가, 현재 잔액: {}", userId, points, user.getPoints());
    }

    @Transactional
    public void subtractPoints(Long userId, Integer points) {
        User user = getUserById(userId);
        user.subtractPoints(points);
        log.debug("사용자 {} 포인트 {} 차감, 현재 잔액: {}", userId, points, user.getPoints());
    }

    @Transactional
    public void suspendUser(Long userId, String reason) {
        User user = getUserById(userId);
        user.suspend();
        log.info("사용자 {} 정지, 사유: {}", userId, reason);
    }

    @Transactional
    public void activateUser(Long userId) {
        User user = getUserById(userId);
        user.activate();
        log.info("사용자 {} 활성화", userId);
    }

    public Page<User> findUsersWithActivity(Pageable pageable) {
        return userRepository.findUsersWithActivity(pageable);
    }

    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsersByKeyword(keyword, pageable);
    }

    public Long countNewUsersThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return userRepository.countNewUsersAfter(startOfMonth);
    }

    public Long countActiveUsersInPeriod(LocalDateTime start, LocalDateTime end) {
        return userRepository.countActiveUsersInPeriod(start, end);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }

    private void validateDuplicateUser(String email, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다: " + email);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + nickname);
        }
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
