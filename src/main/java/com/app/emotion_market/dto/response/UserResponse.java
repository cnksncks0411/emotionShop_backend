package com.app.emotion_market.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private Long userId;
    private String email;
    private String nickname;
    private Integer points;
    private String mbtiType;
    private String profileImage;
    private LocalDateTime createdAt;
}
