package com.team2.nextpage.query.book.service;

import com.team2.nextpage.query.book.dto.request.BookSearchRequest;
import com.team2.nextpage.query.book.dto.response.BookDetailDto;
import com.team2.nextpage.query.book.dto.response.BookDto;
import com.team2.nextpage.query.book.dto.response.BookPageResponse;
import com.team2.nextpage.query.book.dto.response.SentenceDto;
import com.team2.nextpage.query.book.dto.response.SentencePageResponse;
import com.team2.nextpage.query.book.mapper.BookMapper;
import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 소설 Query 서비스 (조회 전용)
 *
 * @author 정진호
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookQueryService {

    private final BookMapper bookMapper;

    /**
     * 소설 검색 및 목록 조회 (페이징/정렬/필터링)
     *
     * @param request 검색 조건 (페이지, 정렬, 필터)
     * @return 페이징된 소설 목록
     */
    public BookPageResponse searchBooks(BookSearchRequest request) {
        // 검색 조건에 맞는 소설 목록 조회
        List<BookDto> books = bookMapper.findBooks(request);

        // 전체 개수 조회 (페이징 정보용)
        Long totalElements = bookMapper.countBooks(request);

        return new BookPageResponse(books, request.getPage(), request.getSize(), totalElements);
    }

    /**
     * 소설 목록 조회 (기본 - 하위 호환용)
     *
     * @return 전체 소설 목록
     * @deprecated searchBooks(BookSearchRequest) 사용 권장
     */
    @Deprecated
    public List<BookDto> searchBooks() {
        return bookMapper.findAllBooks();
    }

    /**
     * 소설 상세 보기 (기본 정보만)
     *
     * @param bookId 소설 ID
     * @return 소설 기본 정보
     * @throws BusinessException 소설을 찾을 수 없는 경우
     */
    public BookDto getBook(Long bookId) {
        BookDto book = bookMapper.findBookDetail(bookId);
        if (book == null) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }
        return book;
    }

    /**
     * 소설 뷰어 모드 조회 (문장 목록 및 투표 통계 포함)
     *
     * @param bookId 소설 ID
     * @return 소설 상세 정보 (문장 목록, 투표 통계 포함)
     * @throws BusinessException 소설을 찾을 수 없는 경우
     */
    public BookDetailDto getBookForViewer(Long bookId) {
        Long userId = com.team2.nextpage.common.util.SecurityUtil.getCurrentUserId();

        // 1. 소설 기본 정보 조회
        BookDetailDto book = bookMapper.findBookForViewer(bookId, userId);
        if (book == null) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }

        // 2. 문장 목록 조회
        List<SentenceDto> sentences = bookMapper.findSentencesByBookId(bookId, userId);
        book.setSentences(sentences);

        // 3. 투표 통계 조회
        book.setLikeCount(bookMapper.countLikes(bookId));
        book.setDislikeCount(bookMapper.countDislikes(bookId));

        return book;
    }

    /**
     * 특정 사용자가 작성한 문장 목록 조회 (페이징)
     */
    public SentencePageResponse getSentencesByUser(Long userId, int page, int size) {
        int offset = page * size;
        List<SentenceDto> sentences = bookMapper.findSentencesByWriterId(userId, offset, size);
        Long totalElements = bookMapper.countSentencesByWriterId(userId);

        return new SentencePageResponse(sentences, page, size, totalElements);
    }
}
