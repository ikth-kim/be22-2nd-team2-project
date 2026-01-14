package com.team2.nextpage.query.member.service;

import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;
import com.team2.nextpage.query.member.dto.response.MemberDto;
import com.team2.nextpage.query.member.mapper.MemberMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberQueryServiceTest {

    @InjectMocks
    private MemberQueryService memberQueryService;

    @Mock
    private MemberMapper memberMapper;

    @Test
    @DisplayName("마이페이지 정보 조회 성공")
    void getMyPage_Success() {
        // given
        String userEmail = "test@test.com";
        Long userId = 1L;

        MemberDto memberDto = new MemberDto();
        memberDto.setUserId(userId);
        memberDto.setUserEmail(userEmail);
        memberDto.setUserNicknm("Tester");

        given(memberMapper.findByUserEmail(userEmail)).willReturn(Optional.of(memberDto));
        given(memberMapper.countCreatedBooks(userId)).willReturn(5);
        given(memberMapper.countWrittenSentences(userId)).willReturn(10);
        given(memberMapper.countWrittenComments(userId)).willReturn(3);

        // when
        MemberDto result = memberQueryService.getMyPage(userEmail);

        // then
        assertThat(result.getUserEmail()).isEqualTo(userEmail);
        assertThat(result.getCreatedBookCount()).isEqualTo(5L);
        assertThat(result.getWrittenSentenceCount()).isEqualTo(10L);
        assertThat(result.getWrittenCommentCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("마이페이지 정보 조회 실패 - 사용자 없음")
    void getMyPage_Fail_NotFound() {
        // given
        String userEmail = "unknown@test.com";
        given(memberMapper.findByUserEmail(userEmail)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberQueryService.getMyPage(userEmail))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }
}
