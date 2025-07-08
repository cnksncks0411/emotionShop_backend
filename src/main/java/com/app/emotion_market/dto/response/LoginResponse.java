package com.app.emotion_market.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private Long userId;
    private String email;
    private String nickname;
    private Integer points;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
}
