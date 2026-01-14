package com.team2.nextpage.websocket.controller;

import com.team2.nextpage.jwt.JwtAuthenticationFilter;
import com.team2.nextpage.jwt.JwtTokenProvider;
import com.team2.nextpage.websocket.dto.TypingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TypingController.class)
@AutoConfigureMockMvc(addFilters = false)
class TypingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("입력 중 상태 테스트")
    void typingStatus_Test() {
        TypingStatus status = new TypingStatus();
        status.setBookId(1L);
    }
}