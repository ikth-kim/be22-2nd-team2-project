package com.team2.storyservice.query.book.mapper;

import com.team2.storyservice.query.book.dto.request.BookSearchRequest;
import com.team2.storyservice.query.book.dto.response.BookDetailDto;
import com.team2.storyservice.query.book.dto.response.BookDto;
import com.team2.storyservice.query.book.dto.response.SentenceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 소설 Query Mapper (검색 필터링/페이징 최적화)
 *
 * @author 정진호
 */
@Mapper
public interface BookMapper {

    /**
     * 소설 목록 조회 (필터링/검색/페이징)
     */
    List<BookDto> findBooks(@Param("request") BookSearchRequest request);

    /**
     * 소설 전체 개수 조회 (페이징용)
     */
    Long countBooks(@Param("request") BookSearchRequest request);

    /**
     * 소설 상세 조회
     */
    BookDto findBookDetail(@Param("bookId") Long bookId);

    /**
     * 소설 상세 조회 (뷰어용 - 완성된 소설만 포함)
     */
    BookDetailDto findBookForViewer(@Param("bookId") Long bookId, @Param("userId") Long userId);

    /**
     * 소설의 문장 목록 조회
     */
    List<SentenceDto> findSentencesByBookId(@Param("bookId") Long bookId, @Param("userId") Long userId);

    /**
     * 기존 메서드 (하위 호환용)
     */
    List<BookDto> findAllBooks();

    /**
     * 특정 사용자가 쓴 문장 목록 조회 (페이징)
     */
    List<SentenceDto> findSentencesByWriterId(@Param("writerId") Long writerId, @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 특정 사용자가 쓴 문장 전체 개수
     */
    Long countSentencesByWriterId(@Param("writerId") Long writerId);
}
