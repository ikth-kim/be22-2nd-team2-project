package com.team2.nextpage.auth.service;

import com.team2.nextpage.command.member.entity.Member;
import com.team2.nextpage.command.member.repository.MemberRepository;
import com.team2.nextpage.config.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security 사용자 인증을 위한 UserDetailsService 구현
 * 이메일을 통해 데이터베이스에서 사용자 정보를 조회합니다.
 *
 * @author 정진호
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  /**
   * 이메일(username)을 통해 사용자 정보 조회
   * 
   * @param email 사용자 이메일
   * @return UserDetails 구현체 (CustomUserDetails)
   * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
   */
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    log.debug("사용자 인증 시도: {}", email);

    Member member = memberRepository.findByUserEmail(email)
        .orElseThrow(() -> {
          log.warn("사용자를 찾을 수 없습니다: {}", email);
          return new UsernameNotFoundException("이메일 또는 비밀번호가 올바르지 않습니다.");
        });

    log.debug("사용자 인증 성공: {} (역할: {})", email, member.getUserRole());
    return new CustomUserDetails(member);
  }

  /**
   * 사용자 ID로 UserDetails 조회 (JWT 토큰 갱신 등에 사용)
   * 
   * @param userId 사용자 ID
   * @return UserDetails 구현체
   * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
   */
  @Transactional(readOnly = true)
  public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
    log.debug("사용자 ID로 조회: {}", userId);

    Member member = memberRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("사용자를 찾을 수 없습니다. ID: {}", userId);
          return new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        });

    return new CustomUserDetails(member);
  }
}
