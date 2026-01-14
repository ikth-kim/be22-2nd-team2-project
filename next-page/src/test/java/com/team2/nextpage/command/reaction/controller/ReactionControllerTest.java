package com.team2.nextpage.command.reaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.nextpage.command.member.entity.Member;
import com.team2.nextpage.command.member.repository.MemberRepository;
import com.team2.nextpage.command.reaction.dto.request.CreateCommentRequest;
import com.team2.nextpage.command.reaction.service.ReactionService;
import com.team2.nextpage.common.util.SecurityUtil;
import com.team2.nextpage.fixtures.RequestDtoTestBuilder;
import com.team2.nextpage.jwt.JwtAuthenticationFilter;
import com.team2.nextpage.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReactionService reactionService;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("댓글 작성 성공 테스트")
    void createComment_Success() throws Exception {
        Long userId = 1L;
        CreateCommentRequest request = RequestDtoTestBuilder.commentRequest(10L, "test comment");
        Member member = mock(Member.class);
        given(member.getUserNicknm()).willReturn("nick");

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            given(reactionService.addComment(any(CreateCommentRequest.class))).willReturn(100L);
            given(memberRepository.findById(userId)).willReturn(Optional.of(member));

            mockMvc.perform(post("/api/reactions/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(100L));
        }
    }
}