package com.team2.nextpage.command.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.nextpage.command.book.dto.request.CreateBookRequest;
import com.team2.nextpage.command.book.dto.request.SentenceAppendRequest;
import com.team2.nextpage.command.book.service.BookService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("소설 생성 성공 테스트")
    void createBook_Success() throws Exception {
        Long userId = 1L;
        Long createdBookId = 10L;
        CreateBookRequest request = RequestDtoTestBuilder.createBookRequest("Title", "FANTASY", "Start", 10);
        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            given(bookService.createBook(eq(userId), any(CreateBookRequest.class))).willReturn(createdBookId);

            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(createdBookId));
        }
    }

    @Test
    @DisplayName("문장 추가 성공 테스트")
    void appendSentence_Success() throws Exception {
        Long userId = 1L;
        Long bookId = 10L;
        Long sentenceId = 100L;
        SentenceAppendRequest request = RequestDtoTestBuilder.sentenceRequest("Next sentence");
        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            given(bookService.appendSentence(eq(bookId), eq(userId), any(SentenceAppendRequest.class)))
                    .willReturn(sentenceId);

            mockMvc.perform(post("/api/books/{bookId}/sentences", bookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}