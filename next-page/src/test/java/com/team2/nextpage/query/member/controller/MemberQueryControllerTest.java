package com.team2.nextpage.query.member.controller;

import com.team2.nextpage.command.member.controller.MemberController;
import com.team2.nextpage.common.util.SecurityUtil;
import com.team2.nextpage.jwt.JwtAuthenticationFilter;
import com.team2.nextpage.jwt.JwtTokenProvider;
import com.team2.nextpage.query.book.controller.BookQueryController;
import com.team2.nextpage.query.member.dto.response.MemberDto;
import com.team2.nextpage.query.member.service.MemberQueryService;
import com.team2.nextpage.query.reaction.controller.ReactionQueryController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberQueryService memberQueryService;

    // Security Mocks
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Controllers needed for HATEOAS
    @MockitoBean
    private BookQueryController bookQueryController;
    @MockitoBean
    private ReactionQueryController reactionQueryController;
    @MockitoBean
    private MemberController memberController;

    @Test
    @DisplayName("마이페이지 조회 성공 테스트")
    void getMyInfo_Success() throws Exception {
        String email = "test@test.com";
        MemberDto dto = new MemberDto();
        dto.setUserEmail(email);
        dto.setUserNicknm("tester");

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserEmail).thenReturn(email);
            given(memberQueryService.getMyPage(anyString())).willReturn(dto);

            mockMvc.perform(get("/api/members/me")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userEmail").value(email));
        }
    }
}