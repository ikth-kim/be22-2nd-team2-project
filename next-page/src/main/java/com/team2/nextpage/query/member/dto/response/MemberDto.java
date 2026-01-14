package com.team2.nextpage.query.member.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.hateoas.RepresentationModel;

/**
 * 회원 조회용 DTO (마이페이지 활동 통계 포함)
 *
 * @author 김태형
 */
@Getter
@Setter
@NoArgsConstructor
public class MemberDto extends RepresentationModel<MemberDto> {
    // 기본 정보
    private Long userId;
    private String userEmail;
    private String userNicknm;
    private String userRole;

    // 활동 통계
    private Integer createdBookCount; // 내가 생성한 소설 수
    private Integer writtenSentenceCount; // 내가 작성한 문장 수
    private Integer writtenCommentCount; // 내가 작성한 댓글 수
}
