package com.team2.nextpage.auth.service;

import com.team2.nextpage.auth.dto.LoginRequest;
import com.team2.nextpage.auth.dto.TokenResponse;
import com.team2.nextpage.auth.entity.RefreshToken;
import com.team2.nextpage.auth.repository.AuthRepository;
import com.team2.nextpage.command.member.entity.Member;
import com.team2.nextpage.command.member.repository.MemberRepository;
import com.team2.nextpage.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 인증(Auth) 서비스
 * 로그인, 토큰 갱신, 로그아웃 등 인증/인가 관련 비즈니스 로직을 처리합니다.
 *
 * @author 김태형
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthRepository authRepository;

  @Value("${jwt.refresh-token-validity-in-seconds:604800}")
  private long refreshTokenValidityInSeconds;

  /**
   * 로그인 처리
   * 사용자 인증 후 Access Token과 Refresh Token을 발급합니다.
   *
   * @param loginRequest 로그인 요청 DTO
   * @return TokenResponse (accessToken, refreshToken)
   * @throws BadCredentialsException 인증 실패 시
   */
  public TokenResponse login(LoginRequest loginRequest) {
    // 1. 사용자 조회
    Member member = memberRepository.findByUserEmail(loginRequest.getUserEmail())
        .orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다."));

    // 2. 비밀번호 검증
    if (!passwordEncoder.matches(loginRequest.getUserPw(), member.getUserPw())) {
      throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
    }

    // 3. Authentication 객체 생성
    Authentication authentication = createAuthentication(member);

    // 4. JWT 토큰 생성
    String accessToken = jwtTokenProvider.createAccessToken(authentication);
    String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

    // 5. Refresh Token 저장 (기존 토큰이 있으면 갱신)
    saveOrUpdateRefreshToken(member.getUserEmail(), refreshToken);

    log.info("로그인 성공: {}", member.getUserEmail());

    return TokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  /**
   * 토큰 갱신
   * Refresh Token을 검증하고 새로운 Access Token과 Refresh Token을 발급합니다.
   *
   * @param providedRefreshToken 클라이언트가 제공한 Refresh Token
   * @return TokenResponse (새로운 accessToken, refreshToken)
   * @throws BadCredentialsException 토큰 검증 실패 시
   */
  public TokenResponse refreshToken(String providedRefreshToken) {
    // 1. Refresh Token 유효성 검증
    if (!jwtTokenProvider.validateToken(providedRefreshToken)) {
      throw new BadCredentialsException("유효하지 않은 Refresh Token입니다.");
    }

    // 2. 토큰에서 사용자 이메일 추출
    String userEmail = jwtTokenProvider.getUserEmailFromToken(providedRefreshToken);

    // 3. 저장된 Refresh Token 조회 및 일치 여부 확인
    RefreshToken storedToken = authRepository.findByUserEmail(userEmail)
        .orElseThrow(() -> new BadCredentialsException("저장된 Refresh Token이 없습니다."));

    if (!storedToken.getToken().equals(providedRefreshToken)) {
      throw new BadCredentialsException("Refresh Token이 일치하지 않습니다.");
    }

    // 4. 토큰 만료 확인
    if (storedToken.isExpired()) {
      authRepository.delete(storedToken);
      throw new BadCredentialsException("Refresh Token이 만료되었습니다. 다시 로그인해주세요.");
    }

    // 5. 사용자 조회
    Member member = memberRepository.findByUserEmail(userEmail)
        .orElseThrow(() -> new BadCredentialsException("사용자를 찾을 수 없습니다."));

    // 6. 새로운 토큰 발급
    Authentication authentication = createAuthentication(member);
    String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

    // 7. Refresh Token 갱신
    saveOrUpdateRefreshToken(userEmail, newRefreshToken);

    log.info("토큰 갱신 성공: {}", userEmail);

    return TokenResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .build();
  }

  /**
   * 로그아웃 처리
   * Refresh Token을 삭제하여 무효화합니다.
   *
   * @param refreshToken 삭제할 Refresh Token
   */
  public void logout(String refreshToken) {
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      log.warn("로그아웃 시 유효하지 않은 토큰 전달됨");
      return;
    }

    String userEmail = jwtTokenProvider.getUserEmailFromToken(refreshToken);
    authRepository.deleteByUserEmail(userEmail);

    log.info("로그아웃 처리 완료: {}", userEmail);
  }

  /**
   * Authentication 객체 생성
   */
  private Authentication createAuthentication(Member member) {
    return new UsernamePasswordAuthenticationToken(
        member.getUserEmail(),
        null,
        Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getUserRole().name())));
  }

  /**
   * Refresh Token 저장 또는 갱신
   */
  private void saveOrUpdateRefreshToken(String userEmail, String token) {
    LocalDateTime expiryDate = LocalDateTime.now()
        .plusSeconds(refreshTokenValidityInSeconds);

    authRepository.findByUserEmail(userEmail)
        .ifPresentOrElse(
            existingToken -> existingToken.updateToken(token, expiryDate),
            () -> authRepository.save(
                RefreshToken.builder()
                    .userEmail(userEmail)
                    .token(token)
                    .expiryDate(expiryDate)
                    .build()));
  }
}
