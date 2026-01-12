package com.team2.nextpage.command.member.controller;

import com.team2.nextpage.command.member.dto.request.SignUpRequest;
import com.team2.nextpage.command.member.service.MemberService;
import com.team2.nextpage.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 Command 컨트롤러
 *
 * @author 김태형
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  // 1. 회원가입
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<String>> signup(@RequestBody @Valid SignUpRequest memberCreateRequest) {
    memberService.registUser(memberCreateRequest); // 서비스에게 가입 처리 위임
    return ResponseEntity.ok(ApiResponse.success("회원가입 성공")); // 성공 응답 반환
  }

  // 2. 관리자
  @PostMapping("/admin")
  public ResponseEntity<ApiResponse<String>> signupAdmin(@RequestBody @Valid SignUpRequest memberCreateRequest) {
    memberService.registAdmin(memberCreateRequest);
    return ResponseEntity.ok(ApiResponse.success("관리자 가입 성공"));
  }
}
