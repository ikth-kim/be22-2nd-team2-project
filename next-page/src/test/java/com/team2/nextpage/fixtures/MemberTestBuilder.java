package com.team2.nextpage.fixtures;

import com.team2.nextpage.command.member.entity.Member;
import com.team2.nextpage.command.member.entity.UserRole;
import com.team2.nextpage.command.member.entity.UserStatus;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Member 엔티티 테스트 빌더
 * ReflectionTestUtils 사용을 최소화하고 가독성 높은 테스트 데이터 생성을 지원합니다.
 */
public class MemberTestBuilder {
    private Long userId = 1L;
    private String userEmail = "test@example.com";
    private String userPw = "encodedPassword123";
    private String userNicknm = "테스터";
    private UserRole userRole = UserRole.USER;
    private UserStatus userStatus = UserStatus.ACTIVE;

    /**
     * 기본 Member 빌더 인스턴스 생성
     */
    public static MemberTestBuilder aMember() {
        return new MemberTestBuilder();
    }

    public MemberTestBuilder withUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public MemberTestBuilder withEmail(String email) {
        this.userEmail = email;
        return this;
    }

    public MemberTestBuilder withPassword(String password) {
        this.userPw = password;
        return this;
    }

    public MemberTestBuilder withNickname(String nickname) {
        this.userNicknm = nickname;
        return this;
    }

    /**
     * 관리자 권한으로 설정
     */
    public MemberTestBuilder asAdmin() {
        this.userRole = UserRole.ADMIN;
        return this;
    }

    /**
     * 일반 사용자 권한으로 설정
     */
    public MemberTestBuilder asUser() {
        this.userRole = UserRole.USER;
        return this;
    }

    /**
     * 삭제된 회원으로 설정
     */
    public MemberTestBuilder deleted() {
        this.userStatus = UserStatus.DELETED;
        return this;
    }

    /**
     * Member 엔티티 생성
     */
    public Member build() {
        Member member = Member.builder()
                .userEmail(userEmail)
                .userPw(userPw)
                .userNicknm(userNicknm)
                .userRole(userRole)
                .userStatus(userStatus)
                .build();

        // ID는 @GeneratedValue이므로 리플렉션으로 설정 (불가피)
        if (userId != null) {
            ReflectionTestUtils.setField(member, "userId", userId);
        }

        return member;
    }
}
