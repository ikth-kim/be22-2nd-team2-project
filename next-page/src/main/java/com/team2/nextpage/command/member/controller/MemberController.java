package com.team2.nextpage.command.member.controller;

import com.team2.nextpage.command.member.dto.request.SignUpRequest;
import com.team2.nextpage.command.member.service.MemberService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 Command 컨트롤러
 * 
 * <p>
 * 회원 가입, 탈퇴, 중복 검증 등 회원 관련 명령(Command) 작업을 처리합니다.
 * </p>
 * <ul>
 * <li>회원가입: 일반 사용자 및 관리자 계정 생성</li>
 * <li>회원 탈퇴: 본인 탈퇴 및 관리자에 의한 강제 탈퇴</li>
 * <li>실시간 검증: 이메일 및 닉네임 중복 체크</li>
 * </ul>
 *
 * @author 김태형
 */
@Tag(name = "Members", description = "회원 관리 API - 회원가입, 탈퇴, 중복 검증")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  /**
   * 회원가입
   * 
   * @param memberCreateRequest 회원가입 요청 정보
   * @return 성공 메시지
   */
  @Operation(summary = "회원가입", description = """
      새로운 일반 사용자 계정을 생성합니다.

      **필수 정보**:
      - 이메일: 유효한 이메일 형식, 중복 불가
      - 비밀번호: 8자 이상, 영문/숫자/특수문자 조합 권장
      - 닉네임: 2-20자, 중복 불가

      **검증 규칙**:
      - 이메일 형식 검증
      - 이메일 중복 검증
      - 닉네임 중복 검증
      - 비밀번호 강도 검증

      **주의사항**:
      - 회원가입 전 `/api/auth/check-email`과 `/api/auth/check-nickname`으로 중복 검증 권장
      - 비밀번호는 BCrypt로 암호화되어 저장됨
      """, security = {} // 인증 불필요
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원가입 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": true,
            "data": "회원가입 성공",
            "error": null
          }
          """))),
      @ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": false,
            "data": null,
            "error": {
              "code": "DUPLICATE_EMAIL",
              "message": "이미 사용 중인 이메일입니다."
            }
          }
          """))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": false,
            "data": null,
            "error": {
              "code": "INVALID_INPUT",
              "message": "이메일 형식이 올바르지 않습니다."
            }
          }
          """)))
  })
  @PostMapping("/signup")
  public ResponseEntity<com.team2.nextpage.common.response.ApiResponse<String>> signup(
      @Parameter(description = "회원가입 요청 정보 (이메일, 비밀번호, 닉네임)", required = true, schema = @Schema(implementation = SignUpRequest.class)) @RequestBody @Valid SignUpRequest memberCreateRequest) {
    memberService.registUser(memberCreateRequest);
    return ResponseEntity.ok(com.team2.nextpage.common.response.ApiResponse.success("회원가입 성공"));
  }

  /**
   * 관리자 계정 생성
   * 
   * @param memberCreateRequest 관리자 계정 생성 요청 정보
   * @return 성공 메시지
   */
  @Operation(summary = "관리자 계정 생성", description = """
      새로운 관리자 계정을 생성합니다.

      **관리자 권한**:
      - 모든 댓글 삭제 가능
      - 모든 사용자 강제 탈퇴 가능
      - 연속 문장 작성 가능 (일반 사용자는 불가)

      **주의사항**:
      - 이 API는 개발/테스트 목적으로만 사용해야 합니다
      - 운영 환경에서는 별도의 관리자 승인 프로세스 필요
      - 일반 사용자와 동일한 검증 규칙 적용
      """, security = {} // 인증 불필요 (개발용)
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "관리자 계정 생성 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": true,
            "data": "관리자 가입 성공",
            "error": null
          }
          """))),
      @ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": false,
            "data": null,
            "error": {
              "code": "DUPLICATE_NICKNAME",
              "message": "이미 사용 중인 닉네임입니다."
            }
          }
          """)))
  })
  @PostMapping("/admin")
  public ResponseEntity<com.team2.nextpage.common.response.ApiResponse<String>> signupAdmin(
      @Parameter(description = "관리자 계정 생성 요청 정보", required = true, schema = @Schema(implementation = SignUpRequest.class)) @RequestBody @Valid SignUpRequest memberCreateRequest) {
    memberService.registAdmin(memberCreateRequest);
    return ResponseEntity.ok(com.team2.nextpage.common.response.ApiResponse.success("관리자 가입 성공"));
  }

  /**
   * 회원 탈퇴 (본인)
   * 
   * @return 성공 응답
   */
  @Operation(summary = "회원 탈퇴", description = """
      현재 로그인한 사용자의 계정을 탈퇴합니다.

      **탈퇴 처리**:
      - 사용자 정보 삭제
      - 작성한 소설, 문장, 댓글은 유지 (익명 처리)
      - Refresh Token 무효화

      **주의사항**:
      - 탈퇴 후 복구 불가능
      - 동일한 이메일로 재가입 가능
      - 탈퇴 후 자동으로 로그아웃됨

      **권장 사항**:
      - 탈퇴 전 사용자에게 확인 절차 필요
      - 탈퇴 후 로그인 페이지로 리다이렉트
      """, security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": true,
            "data": null,
            "error": null
          }
          """))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": false,
            "data": null,
            "error": {
              "code": "UNAUTHENTICATED",
              "message": "인증이 필요합니다."
            }
          }
          """)))
  })
  @DeleteMapping("/withdraw")
  public ResponseEntity<com.team2.nextpage.common.response.ApiResponse<Void>> withdraw() {
    memberService.withdraw();
    return ResponseEntity.ok(com.team2.nextpage.common.response.ApiResponse.success());
  }

  /**
   * 관리자 전용 - 특정 회원 강제 탈퇴
   * 
   * @param userId 탈퇴시킬 회원 ID
   * @return 성공 응답
   */
  @Operation(summary = "회원 강제 탈퇴 (관리자 전용)", description = """
      관리자가 특정 회원을 강제로 탈퇴시킵니다.

      **권한**:
      - 관리자(ADMIN) 권한 필요

      **사용 시나리오**:
      - 약관 위반 사용자 제재
      - 부적절한 콘텐츠 작성자 제재
      - 스팸 계정 삭제

      **처리 내용**:
      - 대상 사용자 계정 삭제
      - 작성한 콘텐츠는 유지 (익명 처리)
      - 대상 사용자의 모든 토큰 무효화

      **주의사항**:
      - 자기 자신은 탈퇴시킬 수 없음
      - 탈퇴 후 복구 불가능
      - 감사 로그 기록 권장
      """, security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원 강제 탈퇴 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": true,
            "data": null,
            "error": null
          }
          """))),
      @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 아님)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": false,
            "data": null,
            "error": {
              "code": "UNAUTHORIZED",
              "message": "관리자 권한이 필요합니다."
            }
          }
          """))),
      @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
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
  @DeleteMapping("/admin/users/{userId}")
  public ResponseEntity<com.team2.nextpage.common.response.ApiResponse<Void>> withdrawByAdmin(
      @Parameter(description = "탈퇴시킬 회원의 ID", required = true, example = "1") @PathVariable Long userId) {
    memberService.withdrawByAdmin(userId);
    return ResponseEntity.ok(com.team2.nextpage.common.response.ApiResponse.success());
  }

  /**
   * 이메일 중복 체크
   * 
   * @param email 검증할 이메일
   * @return 성공 응답 (중복 아님) 또는 에러 (중복)
   */
  @Operation(summary = "이메일 중복 체크", description = """
      회원가입 시 이메일 중복 여부를 실시간으로 검증합니다.

      **사용 시나리오**:
      - 회원가입 폼에서 이메일 입력 시 실시간 검증
      - 회원가입 버튼 클릭 전 최종 검증

      **응답**:
      - 중복 아님: 200 OK
      - 중복: 409 Conflict

      **권장 사항**:
      - 사용자가 이메일 입력을 완료한 후 (onBlur) 호출
      - Debounce 적용하여 불필요한 요청 방지
      """, security = {} // 인증 불필요
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "사용 가능한 이메일", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": true,
            "data": null,
            "error": null
          }
          """))),
      @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": false,
            "data": null,
            "error": {
              "code": "DUPLICATE_EMAIL",
              "message": "이미 사용 중인 이메일입니다."
            }
          }
          """)))
  })
  @GetMapping("/check-email")
  public ResponseEntity<com.team2.nextpage.common.response.ApiResponse<Void>> checkEmail(
      @Parameter(description = "검증할 이메일 주소", required = true, example = "user@example.com") @RequestParam String email) {
    memberService.validateDuplicateEmail(email);
    return ResponseEntity.ok(com.team2.nextpage.common.response.ApiResponse.success());
  }

  /**
   * 닉네임 중복 체크
   * 
   * @param nickname 검증할 닉네임
   * @return 성공 응답 (중복 아님) 또는 에러 (중복)
   */
  @Operation(summary = "닉네임 중복 체크", description = """
      회원가입 시 닉네임 중복 여부를 실시간으로 검증합니다.

      **사용 시나리오**:
      - 회원가입 폼에서 닉네임 입력 시 실시간 검증
      - 회원가입 버튼 클릭 전 최종 검증

      **응답**:
      - 중복 아님: 200 OK
      - 중복: 409 Conflict

      **권장 사항**:
      - 사용자가 닉네임 입력을 완료한 후 (onBlur) 호출
      - Debounce 적용하여 불필요한 요청 방지
      - 2-20자 길이 제한 클라이언트에서 먼저 검증
      """, security = {} // 인증 불필요
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "사용 가능한 닉네임", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": true,
            "data": null,
            "error": null
          }
          """))),
      @ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
          {
            "success": false,
            "data": null,
            "error": {
              "code": "DUPLICATE_NICKNAME",
              "message": "이미 사용 중인 닉네임입니다."
            }
          }
          """)))
  })
  @GetMapping("/check-nickname")
  public ResponseEntity<com.team2.nextpage.common.response.ApiResponse<Void>> checkNickname(
      @Parameter(description = "검증할 닉네임", required = true, example = "홍길동") @RequestParam String nickname) {
    memberService.validateDuplicateNicknm(nickname);
    return ResponseEntity.ok(com.team2.nextpage.common.response.ApiResponse.success());
  }
}
