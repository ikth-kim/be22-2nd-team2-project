package com.team2.memberservice.auth.controller;

import com.team2.memberservice.auth.dto.LoginRequest;
import com.team2.memberservice.auth.dto.TokenResponse;
import com.team2.memberservice.auth.service.AuthService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * 인증(Auth) 컨트롤러
 * 로그인, 토큰 갱신, 로그아웃 등 인증/인가 관련 엔드포인트를 제공합니다.
 * 
 * <p>
 * 이 컨트롤러는 JWT 기반 인증 시스템의 핵심 기능을 제공합니다:
 * </p>
 * <ul>
 * <li>로그인: 이메일/비밀번호로 JWT 토큰 발급</li>
 * <li>토큰 갱신: Refresh Token으로 새로운 Access Token 발급</li>
 * <li>로그아웃: Refresh Token 무효화 및 쿠키 삭제</li>
 * </ul>
 *
 * @author 김태형
 */
@Tag(name = "Authentication", description = "인증 관련 API - 로그인, 토큰 갱신, 로그아웃")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  /**
   * 로그인
   * 사용자 이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.
   *
   * @param loginRequest 로그인 요청 DTO (userEmail, userPw)
   * @return Access Token (Body) + Refresh Token (Cookie)
   */
  @Operation(summary = "로그인", description = """
      사용자 이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.

      **응답 구성**:
      - **Body**: Access Token (JSON)
      - **Cookie**: Refresh Token (HttpOnly, 7일 유효)

      **Access Token**:
      - 유효 기간: 1시간
      - 용도: API 요청 시 Authorization 헤더에 포함

      **Refresh Token**:
      - 유효 기간: 7일
      - 용도: Access Token 갱신
      - 저장 위치: HttpOnly 쿠키 (XSS 공격 방지)

      **주의사항**:
      - 이메일과 비밀번호가 일치하지 않으면 401 에러 반환
      - 존재하지 않는 사용자는 404 에러 반환
      """)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class), examples = @ExampleObject(name = "로그인 성공 응답", value = """
          {
            "success": true,
            "data": {
              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
              "tokenType": "Bearer",
              "expiresIn": 3600
            },
            "error": null
          }
          """))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 - 이메일 또는 비밀번호가 올바르지 않음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": false,
            "data": null,
            "error": {
              "code": "INVALID_CREDENTIALS",
              "message": "이메일 또는 비밀번호가 올바르지 않습니다."
            }
          }
          """))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "로그인 승인 대기 중 (관리자)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": false,
            "data": null,
            "error": {
              "code": "A005",
              "message": "관리자 승인 대기 중인 계정입니다."
            }
          }
          """))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": false,
            "data": null,
            "error": {
              "code": "USER_NOT_FOUND",
              "message": "사용자를 찾을 수 없습니다."
            }
          }
          """)))
  })
  @PostMapping("/login")
  public ResponseEntity<com.team2.commonmodule.response.ApiResponse<TokenResponse>> login(
      @Parameter(description = "로그인 요청 정보 (이메일, 비밀번호)", required = true, schema = @Schema(implementation = LoginRequest.class)) @RequestBody @Valid LoginRequest loginRequest) {
    TokenResponse response = authService.login(loginRequest);
    return buildTokenResponse(response);
  }

  /**
   * 토큰 갱신
   * Refresh Token을 이용하여 새로운 Access Token과 Refresh Token을 발급받습니다.
   *
   * @param refreshToken 쿠키에서 전달받은 Refresh Token
   * @return 새로운 Access Token (Body) + Refresh Token (Cookie)
   */
  @Operation(summary = "토큰 갱신", description = """
      Refresh Token을 이용하여 새로운 Access Token과 Refresh Token을 발급받습니다.

      **사용 시나리오**:
      1. Access Token이 만료되었을 때
      2. 클라이언트가 자동으로 토큰 갱신을 원할 때

      **동작 방식**:
      1. 쿠키에서 Refresh Token 추출
      2. Refresh Token 유효성 검증
      3. 새로운 Access Token 및 Refresh Token 발급
      4. 기존 Refresh Token 무효화 (Refresh Token Rotation)

      **보안**:
      - Refresh Token Rotation: 매번 새로운 Refresh Token 발급
      - 기존 Refresh Token은 자동으로 무효화되어 재사용 방지

      **주의사항**:
      - Refresh Token이 없거나 유효하지 않으면 401 에러 반환
      - 만료된 Refresh Token은 사용 불가
      """)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "토큰 갱신 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class), examples = @ExampleObject(name = "토큰 갱신 성공 응답", value = """
          {
            "success": true,
            "data": {
              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.NEW_TOKEN.signature",
              "tokenType": "Bearer",
              "expiresIn": 3600
            },
            "error": null
          }
          """))),
      @ApiResponse(responseCode = "401", description = "Refresh Token이 없거나 유효하지 않음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": false,
            "data": null,
            "error": {
              "code": "INVALID_TOKEN",
              "message": "유효하지 않은 토큰입니다."
            }
          }
          """)))
  })
  @PostMapping("/refresh")
  public ResponseEntity<com.team2.commonmodule.response.ApiResponse<TokenResponse>> refreshToken(
      @Parameter(description = "쿠키에 저장된 Refresh Token (자동으로 전송됨)", required = false) @CookieValue(name = "refreshToken", required = false) String refreshToken) {
    // 쿠키에 refresh token이 없을 경우
    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // refresh token이 유효하면 새 access/refresh token 발급
    TokenResponse tokenResponse = authService.refreshToken(refreshToken);
    return buildTokenResponse(tokenResponse);
  }

  /**
   * 로그아웃
   * Refresh Token을 무효화하고 쿠키를 삭제합니다.
   *
   * @param refreshToken 쿠키에서 전달받은 Refresh Token
   * @return 성공 응답
   */
  @Operation(summary = "로그아웃", description = """
      Refresh Token을 무효화하고 쿠키를 삭제합니다.

      **동작 방식**:
      1. 쿠키에서 Refresh Token 추출
      2. 데이터베이스에서 해당 Refresh Token 삭제
      3. Refresh Token 쿠키 삭제 (maxAge=0)

      **주의사항**:
      - Access Token은 서버에서 무효화할 수 없음 (Stateless)
      - Access Token은 만료 시간까지 유효하므로 클라이언트에서 삭제 필요
      - Refresh Token만 서버에서 무효화 가능

      **권장 사항**:
      - 로그아웃 후 클라이언트는 Access Token을 즉시 삭제해야 함
      - 로그인 페이지로 리다이렉트 권장
      """)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "로그아웃 성공 응답", value = """
          {
            "success": true,
            "data": null,
            "error": null
          }
          """)))
  })
  @PostMapping("/logout")
  public ResponseEntity<com.team2.commonmodule.response.ApiResponse<Void>> logout(
      @Parameter(description = "쿠키에 저장된 Refresh Token (자동으로 전송됨)", required = false) @CookieValue(name = "refreshToken", required = false) String refreshToken) {
    if (refreshToken != null) {
      authService.logout(refreshToken);
    }
    ResponseCookie deleteCookie = createDeleteRefreshTokenCookie();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
        .body(com.team2.commonmodule.response.ApiResponse.success());
  }

  /**
   * Access Token과 Refresh Token을 body와 쿠키에 담아 반환
   */
  private ResponseEntity<com.team2.commonmodule.response.ApiResponse<TokenResponse>> buildTokenResponse(
      TokenResponse tokenResponse) {
    ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.getRefreshToken());
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(com.team2.commonmodule.response.ApiResponse.success(tokenResponse));
  }

  /**
   * Refresh Token 쿠키 생성
   */
  private ResponseCookie createRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true) // HttpOnly 속성 설정 (JavaScript에서 접근 불가)
        // .secure(true) // HTTPS 환경일 때만 전송 (운영 환경에서 활성화 권장)
        .path("/") // 쿠키 범위: 전체 경로
        .maxAge(java.util.Objects.requireNonNull(Duration.ofDays(7))) // 쿠키 만료 기간: 7일
        .sameSite("Strict") // CSRF 공격 방어를 위한 SameSite 설정
        .build();
  }

  /**
   * Refresh Token 삭제용 쿠키 생성 (maxAge = 0)
   */
  private ResponseCookie createDeleteRefreshTokenCookie() {
    return ResponseCookie.from("refreshToken", "")
        .httpOnly(true)
        .path("/")
        .maxAge(0) // 즉시 만료
        .sameSite("Strict")
        .build();
  }
}
