package com.team2.nextpage.query.book.controller;

import com.team2.nextpage.command.book.controller.BookController;
import com.team2.nextpage.command.reaction.controller.ReactionController;
import com.team2.nextpage.jwt.JwtAuthenticationFilter;
import com.team2.nextpage.jwt.JwtTokenProvider;
import com.team2.nextpage.query.book.dto.request.BookSearchRequest;
import com.team2.nextpage.query.book.dto.response.BookDto;
import com.team2.nextpage.query.book.dto.response.BookPageResponse;
import com.team2.nextpage.query.book.service.BookQueryService;
import com.team2.nextpage.query.reaction.controller.ReactionQueryController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookQueryService bookQueryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Controllers for HATEOAS
    @MockitoBean
    private ReactionQueryController reactionQueryController;
    @MockitoBean
    private ReactionController reactionController;
    @MockitoBean
    private BookController bookController;

    @Test
    @DisplayName("소설 목록 조회 성공 테스트")
    void listBooks_Success() throws Exception {
        BookDto book = new BookDto();
        book.setBookId(1L);
        book.setTitle("Test Book");
        BookPageResponse response = new BookPageResponse(List.of(book), 0, 10, 1L);

        given(bookQueryService.searchBooks(any(BookSearchRequest.class))).willReturn(response);

        mockMvc.perform(get("/api/books")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].bookId").value(1L));
    }
}