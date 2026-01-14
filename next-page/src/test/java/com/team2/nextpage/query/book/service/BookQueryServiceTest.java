package com.team2.nextpage.query.book.service;

import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;
import com.team2.nextpage.common.util.SecurityUtil;
import com.team2.nextpage.query.book.dto.request.BookSearchRequest;
import com.team2.nextpage.query.book.dto.response.BookDetailDto;
import com.team2.nextpage.query.book.dto.response.BookDto;
import com.team2.nextpage.query.book.dto.response.BookPageResponse;
import com.team2.nextpage.query.book.dto.response.SentenceDto;
import com.team2.nextpage.query.book.dto.response.SentencePageResponse;
import com.team2.nextpage.query.book.mapper.BookMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BookQueryServiceTest {

    @InjectMocks
    private BookQueryService bookQueryService;

    @Mock
    private BookMapper bookMapper;

    private static MockedStatic<SecurityUtil> securityUtil;

    @BeforeAll
    public static void beforeAll() {
        securityUtil = org.mockito.Mockito.mockStatic(SecurityUtil.class);
    }

    @AfterAll
    public static void afterAll() {
        securityUtil.close();
    }

    @Test
    @DisplayName("소설 검색 및 목록 조회 성공")
    void searchBooks_Success() {
        // given
        BookSearchRequest request = new BookSearchRequest();
        request.setPage(0);
        request.setSize(10);

        BookDto bookDto = new BookDto();
        bookDto.setBookId(1L);
        bookDto.setTitle("Test Title");

        List<BookDto> books = Collections.singletonList(bookDto);

        given(bookMapper.findBooks(request)).willReturn(books);
        given(bookMapper.countBooks(request)).willReturn(1L);

        // when
        BookPageResponse response = bookQueryService.searchBooks(request);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("Test Title");
        assertThat(response.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("소설 상세 보기 성공")
    void getBook_Success() {
        // given
        Long bookId = 1L;
        BookDto bookDto = new BookDto();
        bookDto.setBookId(bookId);
        bookDto.setTitle("Test Title");

        given(bookMapper.findBookDetail(bookId)).willReturn(bookDto);

        // when
        BookDto result = bookQueryService.getBook(bookId);

        // then
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getTitle()).isEqualTo("Test Title");
    }

    @Test
    @DisplayName("소설 상세 보기 실패 - 존재하지 않음")
    void getBook_Fail_NotFound() {
        // given
        Long bookId = 999L;
        given(bookMapper.findBookDetail(bookId)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> bookQueryService.getBook(bookId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("뷰어 모드 조회 성공")
    void getBookForViewer_Success() {
        // given
        Long bookId = 1L;
        Long userId = 100L;

        BookDetailDto bookDetailDto = new BookDetailDto();
        bookDetailDto.setBookId(bookId);
        bookDetailDto.setTitle("Viewer Title");

        SentenceDto sentenceDto = new SentenceDto();
        sentenceDto.setSentenceId(10L);
        sentenceDto.setContent("First Sentence");

        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(bookMapper.findBookForViewer(bookId, userId)).willReturn(bookDetailDto);
        given(bookMapper.findSentencesByBookId(bookId, userId)).willReturn(Collections.singletonList(sentenceDto));
        given(bookMapper.countLikes(bookId)).willReturn(5);
        given(bookMapper.countDislikes(bookId)).willReturn(0);

        // when
        BookDetailDto result = bookQueryService.getBookForViewer(bookId);

        // then
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getTitle()).isEqualTo("Viewer Title");
        assertThat(result.getSentences()).hasSize(1);
        assertThat(result.getSentences().get(0).getContent()).isEqualTo("First Sentence");
        assertThat(result.getLikeCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("뷰어 모드 조회 실패 - 존재하지 않음")
    void getBookForViewer_Fail_NotFound() {
        // given
        Long bookId = 999L;
        Long userId = 100L;

        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(bookMapper.findBookForViewer(bookId, userId)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> bookQueryService.getBookForViewer(bookId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("사용자 작성 문장 조회 성공")
    void getSentencesByUser_Success() {
        // given
        Long userId = 10L;
        int page = 0;
        int size = 10;

        SentenceDto sentenceDto = new SentenceDto();
        sentenceDto.setSentenceId(100L);
        sentenceDto.setContent("My Sentence");

        List<SentenceDto> sentences = Collections.singletonList(sentenceDto);

        given(bookMapper.findSentencesByWriterId(userId, 0, 10)).willReturn(sentences);
        given(bookMapper.countSentencesByWriterId(userId)).willReturn(1L);

        // when
        SentencePageResponse response = bookQueryService.getSentencesByUser(userId, page, size);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getContent()).isEqualTo("My Sentence");
        assertThat(response.getTotalElements()).isEqualTo(1L);
    }
}
