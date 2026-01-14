package com.team2.nextpage.query.reaction.controller;

import com.team2.nextpage.command.reaction.controller.ReactionController;
import com.team2.nextpage.common.util.SecurityUtil;
import com.team2.nextpage.jwt.JwtAuthenticationFilter;
import com.team2.nextpage.jwt.JwtTokenProvider;
import com.team2.nextpage.query.reaction.dto.response.CommentDto;
import com.team2.nextpage.query.reaction.service.ReactionQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReactionQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReactionQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReactionQueryService reactionQueryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Controllers for HATEOAS
    @MockitoBean
    private ReactionController reactionController;

    @Test
    @DisplayName("댓글 목록 조회 성공 테스트")
    void getComments_Success() throws Exception {
        CommentDto dto = new CommentDto();
        dto.setCommentId(100L);
        dto.setContent("test comment");

        given(reactionQueryService.getComments(anyLong())).willReturn(List.of(dto));

        mockMvc.perform(get("/api/reactions/comments/10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].commentId").value(100L));
    }

    @Test
    @DisplayName("내가 작성한 댓글 조회 성공 테스트")
    void getMyComments_Success() throws Exception {
        Long userId = 1L;
        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            mockMvc.perform(get("/api/reactions/mycomments")
                    .param("page", "0")
                    .param("size", "10")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}