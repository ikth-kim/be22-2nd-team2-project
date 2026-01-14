package com.team2.nextpage.command.member.service;

import com.team2.nextpage.command.member.dto.request.SignUpRequest;
import com.team2.nextpage.command.member.entity.Member;
import com.team2.nextpage.command.member.entity.UserRole;
import com.team2.nextpage.command.member.entity.UserStatus;
import com.team2.nextpage.command.member.repository.MemberRepository;
import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;
import com.team2.nextpage.common.util.SecurityUtil;
import com.team2.nextpage.fixtures.MemberTestBuilder;
import com.team2.nextpage.fixtures.RequestDtoTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private static MockedStatic<SecurityUtil> securityUtil;

    @BeforeAll
    public static void beforeAll() {
        securityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterAll
    public static void afterAll() {
        securityUtil.close();
    }

    @Test
    @DisplayName("회원가입 성공 - 일반 유저")
    void registUser_Success() {
        // given
        SignUpRequest request = RequestDtoTestBuilder.createSignUpRequest(
                "test@test.com", "password", "nickname"
        );
        given(memberRepository.findByUserEmail("test@test.com")).willReturn(Optional.empty());
        given(memberRepository.existsByUserNicknm("nickname")).willReturn(false);
        given(passwordEncoder.encode("password")).willReturn("encodedPassword");

        // when
        memberService.registUser(request);

        // then
        verify(memberRepository, times(1)).save(argThat(member ->
                member.getUserEmail().equals("test@test.com") &&
                member.getUserPw().equals("encodedPassword") &&
                member.getUserNicknm().equals("nickname") &&
                member.getUserRole() == UserRole.USER &&
                member.getUserStatus() == UserStatus.ACTIVE
        ));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void registUser_Fail_DuplicateEmail() {
        // given
        SignUpRequest request = RequestDtoTestBuilder.createSignUpRequest(
                "duplicate@test.com", "password", "nickname"
        );
        Member existingMember = MemberTestBuilder.aMember()
                .withEmail("duplicate@test.com")
                .build();
        given(memberRepository.findByUserEmail("duplicate@test.com"))
                .willReturn(Optional.of(existingMember));

        // when & then
        assertThatThrownBy(() -> memberService.registUser(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);

        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 닉네임 중복")
    void registUser_Fail_DuplicateNickname() {
        // given
        SignUpRequest request = RequestDtoTestBuilder.createSignUpRequest(
                "test@test.com", "password", "duplicateNick"
        );
        given(memberRepository.findByUserEmail("test@test.com")).willReturn(Optional.empty());
        given(memberRepository.existsByUserNicknm("duplicateNick")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.registUser(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);

        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 성공 - 비밀번호 암호화 확인")
    void registUser_PasswordEncoded() {
        // given
        SignUpRequest request = RequestDtoTestBuilder.createSignUpRequest(
                "test@test.com", "plainPassword", "nickname"
        );
        given(memberRepository.findByUserEmail(anyString())).willReturn(Optional.empty());
        given(memberRepository.existsByUserNicknm(anyString())).willReturn(false);
        given(passwordEncoder.encode("plainPassword")).willReturn("$2a$10$encodedHash");

        // when
        memberService.registUser(request);

        // then
        verify(passwordEncoder, times(1)).encode("plainPassword");
        verify(memberRepository, times(1)).save(argThat(member ->
                member.getUserPw().equals("$2a$10$encodedHash")
        ));
    }

    @Test
    @DisplayName("관리자 등록 성공")
    void registAdmin_Success() {
        // given
        SignUpRequest request = RequestDtoTestBuilder.createSignUpRequest(
                "admin@test.com", "password", "adminNick"
        );
        given(memberRepository.findByUserEmail("admin@test.com")).willReturn(Optional.empty());
        given(memberRepository.existsByUserNicknm("adminNick")).willReturn(false);
        given(passwordEncoder.encode("password")).willReturn("encodedPassword");

        // when
        memberService.registAdmin(request);

        // then
        verify(memberRepository, times(1)).save(argThat(member ->
                member.getUserEmail().equals("admin@test.com") &&
                member.getUserRole() == UserRole.ADMIN &&
                member.getUserStatus() == UserStatus.ACTIVE
        ));
    }

    @Test
    @DisplayName("관리자 등록 실패 - 이메일 중복")
    void registAdmin_Fail_DuplicateEmail() {
        // given
        SignUpRequest request = RequestDtoTestBuilder.createSignUpRequest(
                "duplicate@test.com", "password", "adminNick"
        );
        Member existingMember = MemberTestBuilder.aMember()
                .withEmail("duplicate@test.com")
                .build();
        given(memberRepository.findByUserEmail("duplicate@test.com"))
                .willReturn(Optional.of(existingMember));

        // when & then
        assertThatThrownBy(() -> memberService.registAdmin(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("회원탈퇴 성공")
    void withdraw_Success() {
        // given
        String userEmail = "test@test.com";
        Member member = MemberTestBuilder.aMember()
                .withEmail(userEmail)
                .withNickname("testUser")
                .build();

        given(SecurityUtil.getCurrentUserEmail()).willReturn(userEmail);
        given(memberRepository.findByUserEmail(userEmail)).willReturn(Optional.of(member));

        // when
        memberService.withdraw();

        // then
        verify(memberRepository, times(1)).delete(member);
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 회원 정보 없음")
    void withdraw_Fail_MemberNotFound() {
        // given
        String userEmail = "notfound@test.com";
        given(SecurityUtil.getCurrentUserEmail()).willReturn(userEmail);
        given(memberRepository.findByUserEmail(userEmail)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.withdraw())
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("관리자 권한 회원 강제 탈퇴 - 성공")
    void withdrawByAdmin_Success() {
        // given
        Long targetId = 2L;
        Member targetMember = MemberTestBuilder.aMember()
                .withUserId(targetId)
                .withEmail("target@test.com")
                .withNickname("targetUser")
                .build();

        given(SecurityUtil.isAdmin()).willReturn(true);
        given(memberRepository.findById(targetId)).willReturn(Optional.of(targetMember));

        // when
        memberService.withdrawByAdmin(targetId);

        // then
        verify(memberRepository, times(1)).delete(targetMember);
    }

    @Test
    @DisplayName("관리자 권한 회원 강제 탈퇴 - 실패(권한없음)")
    void withdrawByAdmin_Fail_AccessDenied() {
        // given
        Long targetId = 2L;
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when & then
        assertThatThrownBy(() -> memberService.withdrawByAdmin(targetId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);

        verify(memberRepository, never()).findById(anyLong());
        verify(memberRepository, never()).delete(any(Member.class));
    }

    @Test
    @DisplayName("관리자 권한 회원 강제 탈퇴 - 실패(회원 없음)")
    void withdrawByAdmin_Fail_MemberNotFound() {
        // given
        Long targetId = 999L;
        given(SecurityUtil.isAdmin()).willReturn(true);
        given(memberRepository.findById(targetId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.withdrawByAdmin(targetId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

        verify(memberRepository, never()).delete(any(Member.class));
    }

    @Test
    @DisplayName("이메일 중복 검증 - 중복 없음")
    void validateDuplicateEmail_NoDuplicate() {
        // given
        String email = "unique@test.com";
        given(memberRepository.findByUserEmail(email)).willReturn(Optional.empty());

        // when & then (예외가 발생하지 않아야 함)
        memberService.validateDuplicateEmail(email);

        verify(memberRepository, times(1)).findByUserEmail(email);
    }

    @Test
    @DisplayName("이메일 중복 검증 - 중복 있음")
    void validateDuplicateEmail_Duplicate() {
        // given
        String email = "duplicate@test.com";
        Member existingMember = MemberTestBuilder.aMember()
                .withEmail(email)
                .build();
        given(memberRepository.findByUserEmail(email)).willReturn(Optional.of(existingMember));

        // when & then
        assertThatThrownBy(() -> memberService.validateDuplicateEmail(email))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("닉네임 중복 검증 - 중복 없음")
    void validateDuplicateNicknm_NoDuplicate() {
        // given
        String nickname = "uniqueNick";
        given(memberRepository.existsByUserNicknm(nickname)).willReturn(false);

        // when & then (예외가 발생하지 않아야 함)
        memberService.validateDuplicateNicknm(nickname);

        verify(memberRepository, times(1)).existsByUserNicknm(nickname);
    }

    @Test
    @DisplayName("닉네임 중복 검증 - 중복 있음")
    void validateDuplicateNicknm_Duplicate() {
        // given
        String nickname = "duplicateNick";
        given(memberRepository.existsByUserNicknm(nickname)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.validateDuplicateNicknm(nickname))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);
    }
}
