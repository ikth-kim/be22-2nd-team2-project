package com.team2.nextpage.auth.service;

import com.team2.nextpage.auth.dto.LoginRequest;
import com.team2.nextpage.auth.dto.TokenResponse;
import com.team2.nextpage.auth.entity.RefreshToken;
import com.team2.nextpage.auth.repository.AuthRepository;
import com.team2.nextpage.command.member.entity.Member;
import com.team2.nextpage.command.member.entity.UserRole;
import com.team2.nextpage.command.member.repository.MemberRepository;
import com.team2.nextpage.fixtures.AuthTestBuilder;
import com.team2.nextpage.fixtures.MemberTestBuilder;
import com.team2.nextpage.fixtures.RequestDtoTestBuilder;
import com.team2.nextpage.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthRepository authRepository;

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        LoginRequest request = RequestDtoTestBuilder.createLoginRequest("test@test.com", "password");
        Member member = MemberTestBuilder.aMember()
                .withEmail("test@test.com")
                .withPassword("encodedPassword")
                .build();

        given(memberRepository.findByUserEmail("test@test.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("password", "encodedPassword")).willReturn(true);
        given(jwtTokenProvider.createAccessToken(any(Authentication.class))).willReturn("accessToken");
        given(jwtTokenProvider.createRefreshToken(any(Authentication.class))).willReturn("refreshToken");

        ReflectionTestUtils.setField(authService, "refreshTokenValidityInSeconds", 604800L);

        // when
        TokenResponse response = authService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        verify(authRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_Fail_EmailNotFound() {
        // given
        LoginRequest request = RequestDtoTestBuilder.createLoginRequest("wrong@test.com", "password");
        given(memberRepository.findByUserEmail("wrong@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("아이디 또는 비밀번호가 일치하지 않습니다.");

        verify(authRepository, never()).save(any());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_PasswordMismatch() {
        // given
        LoginRequest request = RequestDtoTestBuilder.createLoginRequest("test@test.com", "wrongPassword");
        Member member = MemberTestBuilder.aMember()
                .withEmail("test@test.com")
                .withPassword("encodedPassword")
                .build();

        given(memberRepository.findByUserEmail("test@test.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("아이디 또는 비밀번호가 일치하지 않습니다.");

        verify(authRepository, never()).save(any());
    }

    @Test
    @DisplayName("토큰 갱신 성공")
    void refreshToken_Success() {
        // given
        String oldRefreshToken = "oldRefreshToken";
        String userEmail = "test@test.com";
        Member member = MemberTestBuilder.aMember()
                .withEmail(userEmail)
                .build();
        RefreshToken storedToken = AuthTestBuilder.aRefreshToken()
                .withToken(oldRefreshToken)
                .withUserEmail(userEmail)
                .build();

        given(jwtTokenProvider.validateToken(oldRefreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserEmailFromToken(oldRefreshToken)).willReturn(userEmail);
        given(authRepository.findByUserEmail(userEmail)).willReturn(Optional.of(storedToken));
        given(memberRepository.findByUserEmail(userEmail)).willReturn(Optional.of(member));
        given(jwtTokenProvider.createAccessToken(any(Authentication.class))).willReturn("newAccessToken");
        given(jwtTokenProvider.createRefreshToken(any(Authentication.class))).willReturn("newRefreshToken");

        ReflectionTestUtils.setField(authService, "refreshTokenValidityInSeconds", 604800L);

        // when
        TokenResponse response = authService.refreshToken(oldRefreshToken);

        // then
        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 토큰")
    void refreshToken_Fail_InvalidToken() {
        // given
        String invalidToken = "invalidToken";
        given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.refreshToken(invalidToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("유효하지 않은 Refresh Token입니다.");

        verify(authRepository, never()).findByUserEmail(anyString());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 저장된 토큰 없음")
    void refreshToken_Fail_NoStoredToken() {
        // given
        String refreshToken = "refreshToken";
        String userEmail = "test@test.com";

        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserEmailFromToken(refreshToken)).willReturn(userEmail);
        given(authRepository.findByUserEmail(userEmail)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("저장된 Refresh Token이 없습니다.");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 토큰 불일치")
    void refreshToken_Fail_TokenMismatch() {
        // given
        String providedToken = "providedToken";
        String storedTokenValue = "differentToken";
        String userEmail = "test@test.com";

        RefreshToken storedToken = AuthTestBuilder.aRefreshToken()
                .withToken(storedTokenValue)
                .withUserEmail(userEmail)
                .build();

        given(jwtTokenProvider.validateToken(providedToken)).willReturn(true);
        given(jwtTokenProvider.getUserEmailFromToken(providedToken)).willReturn(userEmail);
        given(authRepository.findByUserEmail(userEmail)).willReturn(Optional.of(storedToken));

        // when & then
        assertThatThrownBy(() -> authService.refreshToken(providedToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Refresh Token이 일치하지 않습니다.");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 만료된 토큰")
    void refreshToken_Fail_ExpiredToken() {
        // given
        String expiredToken = "expiredToken";
        String userEmail = "test@test.com";

        RefreshToken storedToken = AuthTestBuilder.aRefreshToken()
                .withToken(expiredToken)
                .withUserEmail(userEmail)
                .expired()
                .build();

        given(jwtTokenProvider.validateToken(expiredToken)).willReturn(true);
        given(jwtTokenProvider.getUserEmailFromToken(expiredToken)).willReturn(userEmail);
        given(authRepository.findByUserEmail(userEmail)).willReturn(Optional.of(storedToken));

        // when & then
        assertThatThrownBy(() -> authService.refreshToken(expiredToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Refresh Token이 만료되었습니다. 다시 로그인해주세요.");

        verify(authRepository, times(1)).delete(storedToken);
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // given
        String refreshToken = "refreshToken";
        String userEmail = "test@test.com";

        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserEmailFromToken(refreshToken)).willReturn(userEmail);

        // when
        authService.logout(refreshToken);

        // then
        verify(authRepository, times(1)).deleteByUserEmail(userEmail);
    }

    @Test
    @DisplayName("로그아웃 - 유효하지 않은 토큰 (무시)")
    void logout_InvalidToken_Ignored() {
        // given
        String invalidToken = "invalidToken";
        given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false);

        // when
        authService.logout(invalidToken);

        // then (예외가 발생하지 않고 deleteByUserEmail이 호출되지 않음)
        verify(authRepository, never()).deleteByUserEmail(anyString());
    }

    @Test
    @DisplayName("로그인 성공 - RefreshToken 기존 토큰 갱신")
    void login_Success_UpdateExistingRefreshToken() {
        // given
        LoginRequest request = RequestDtoTestBuilder.createLoginRequest("test@test.com", "password");
        Member member = MemberTestBuilder.aMember()
                .withEmail("test@test.com")
                .withPassword("encodedPassword")
                .build();

        RefreshToken existingToken = AuthTestBuilder.aRefreshToken()
                .withToken("oldToken")
                .withUserEmail("test@test.com")
                .build();

        given(memberRepository.findByUserEmail("test@test.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("password", "encodedPassword")).willReturn(true);
        given(jwtTokenProvider.createAccessToken(any(Authentication.class))).willReturn("accessToken");
        given(jwtTokenProvider.createRefreshToken(any(Authentication.class))).willReturn("newRefreshToken");
        given(authRepository.findByUserEmail("test@test.com")).willReturn(Optional.of(existingToken));

        ReflectionTestUtils.setField(authService, "refreshTokenValidityInSeconds", 604800L);

        // when
        TokenResponse response = authService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");
        verify(authRepository, never()).save(any(RefreshToken.class)); // 갱신이므로 save 호출 안됨
    }
}
