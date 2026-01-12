package com.team2.nextpage.command.member.service;

import com.team2.nextpage.command.member.dto.request.SignUpRequest;
import com.team2.nextpage.command.member.entity.Member;
import com.team2.nextpage.command.member.entity.UserRole;
import com.team2.nextpage.command.member.entity.UserStatus;
import com.team2.nextpage.command.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 회원 Command 서비스 (회원가입, 탈퇴 등)
 *
 * @author 김태형
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

  private final MemberRepository memberRepository;
  private final ModelMapper modelMapper;
  private final PasswordEncoder passwordEncoder;

  /* USER 등록 */
  public void registUser(SignUpRequest memberCreateRequest) {
    if (memberRepository.findByUserEmail(memberCreateRequest.userEmail()).isPresent()) {
      throw new RuntimeException("이미 존재하는 아이디(이메일)입니다.");
    }

    Member member = Member.builder()
        .userEmail(memberCreateRequest.userEmail())
        .userPw(passwordEncoder.encode(memberCreateRequest.userPw()))
        .userNicknm(memberCreateRequest.userNicknm())
        .userRole(UserRole.USER)
        .userStatus(UserStatus.ACTIVE)
        .build();

    memberRepository.save(member);
  }

  /* ADMIN 등록 */
  public void registAdmin(SignUpRequest memberCreateRequest) {
    if (memberRepository.findByUserEmail(memberCreateRequest.userEmail()).isPresent()) {
      throw new RuntimeException("이미 존재하는 아이디(이메일)입니다.");
    }

    Member member = Member.builder()
        .userEmail(memberCreateRequest.userEmail())
        .userPw(passwordEncoder.encode(memberCreateRequest.userPw()))
        .userNicknm(memberCreateRequest.userNicknm())
        .userRole(UserRole.ADMIN)
        .userStatus(UserStatus.ACTIVE)
        .build();

    memberRepository.save(member);
  }

  // 회원 탈퇴
  public void withdraw() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userEmail = authentication.getName(); // 인증 시 userEmail을 사용

    Member member = memberRepository.findByUserEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("로그인 유저 정보가 없습니다."));

    // Soft Delete 수행
    memberRepository.delete(member);
  }
}
