package com.team2.nextpage.command.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 DTO
 *
 * @author 김태형
 */
public record SignUpRequest(
    @NotBlank(message = "이메일은 필수입니다.") @Size(min = 4, max = 100, message = "이메일은 100자 이하로 입력해주세요.") String userEmail,

    @NotBlank(message = "비밀번호는 필수입니다.") String userPw,

    @NotBlank(message = "닉네임은 필수입니다.") String userNicknm) {
}
