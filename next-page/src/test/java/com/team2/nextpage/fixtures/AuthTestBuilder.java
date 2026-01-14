package com.team2.nextpage.fixtures;

import com.team2.nextpage.auth.entity.RefreshToken;

import java.time.LocalDateTime;

/**
 * RefreshToken 엔티티 테스트 빌더
 * 인증 관련 엔티티 생성을 간편하게 지원합니다.
 */
public class AuthTestBuilder {
    private String userEmail = "test@example.com";
    private String token = "refresh-token-xyz123";
    private LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);

    /**
     * 기본 RefreshToken 빌더 인스턴스 생성
     */
    public static AuthTestBuilder aRefreshToken() {
        return new AuthTestBuilder();
    }

    public AuthTestBuilder withUserEmail(String userEmail) {
        this.userEmail = userEmail;
        return this;
    }

    public AuthTestBuilder withToken(String token) {
        this.token = token;
        return this;
    }

    public AuthTestBuilder withExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    /**
     * 만료된 토큰으로 설정
     */
    public AuthTestBuilder expired() {
        this.expiryDate = LocalDateTime.now().minusDays(1);
        return this;
    }

    /**
     * RefreshToken 엔티티 생성
     */
    public RefreshToken build() {
        return RefreshToken.builder()
                .userEmail(userEmail)
                .token(token)
                .expiryDate(expiryDate)
                .build();
    }
}
