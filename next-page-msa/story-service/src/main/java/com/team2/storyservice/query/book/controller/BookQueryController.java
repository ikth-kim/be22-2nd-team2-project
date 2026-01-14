package com.team2.storyservice.query.book.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import com.team2.storyservice.command.book.controller.BookController;
// Cross-service HATEOAS links are not directly supported in this service.
// Clients should construct links via API Gateway.

import com.team2.storyservice.query.book.dto.request.BookSearchRequest;
import com.team2.storyservice.query.book.dto.response.BookDetailDto;
import com.team2.storyservice.query.book.dto.response.BookDto;
import com.team2.storyservice.query.book.dto.response.BookPageResponse;
import com.team2.storyservice.query.book.dto.response.SentenceDto;
import com.team2.storyservice.query.book.dto.response.SentencePageResponse;
import com.team2.storyservice.query.book.service.BookQueryService;
import com.team2.commonmodule.response.ApiResponse;
import com.team2.commonmodule.util.SecurityUtil;
import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 소설 Query 컨트롤러
 *
 * @author 정진호
 */
@Tag(name = "Book Queries", description = "소설 조회(Query) API")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class BookQueryController {

    private final BookQueryService bookQueryService;

    /**
     * 소설 목록 조회 API (페이지네이션/필터링)
     * GET /api/books
     *
     * @param request 검색 조건 (page, size, sortBy, sortOrder, status, categoryId,
     *                keyword)
     * @return 페이징된 소설 목록
     */
    @Operation(summary = "소설 목록 조회", description = "조건에 따라 소설 목록을 페이징하여 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<BookPageResponse>> list(@ModelAttribute BookSearchRequest request) {
        BookPageResponse result = bookQueryService.searchBooks(request);
        if (result.getContent() != null) {
            for (BookDto book : result.getContent()) {
                book.add(linkTo(methodOn(BookQueryController.class).detail(book.getBookId())).withSelfRel());
                book.add(linkTo(methodOn(BookQueryController.class).view(book.getBookId())).withRel("view"));
            }
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 소설 상세 조회 API
     * GET /api/books/{bookId}
     *
     * @param bookId 소설 ID
     * @return 소설 기본 정보
     */
    @Operation(summary = "소설 상세 조회", description = "소설의 기본 정보를 조회합니다.")
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookDto>> detail(@PathVariable Long bookId) {
        BookDto book = bookQueryService.getBook(bookId);

        // HATEOAS Links (cross-service links removed for MSA)
        book.add(linkTo(methodOn(BookQueryController.class).detail(bookId)).withSelfRel());
        book.add(linkTo(methodOn(BookQueryController.class).view(bookId)).withRel("view"));
        // Cross-service links (e.g. to reactions) are handled by API Gateway or
        // Frontend

        return ResponseEntity.ok().body(ApiResponse.success(book));
    }

    /**
     * 소설 뷰어 모드 조회 API (문장 목록 포함)
     * GET /api/books/{bookId}/view
     *
     * @param bookId 소설 ID
     * @return 소설 상세 정보 (문장 목록, 투표 통계 포함)
     */
    @Operation(summary = "소설 뷰어 조회", description = "소설의 전체 문장을 포함하여 읽기 모드로 조회합니다.")
    @GetMapping("/{bookId}/view")
    public ResponseEntity<ApiResponse<BookDetailDto>> view(@PathVariable Long bookId) {
        BookDetailDto bookDetail = bookQueryService.getBookForViewer(bookId);

        // HATEOAS Links (cross-service links removed for MSA)
        bookDetail.add(linkTo(methodOn(BookQueryController.class).view(bookId)).withSelfRel());
        bookDetail.add(linkTo(methodOn(BookQueryController.class).detail(bookId)).withRel("detail"));
        // Cross-service links handled by API Gateway

        if ("IN_PROGRESS".equals(bookDetail.getStatus())) {
            bookDetail.add(linkTo(methodOn(BookController.class).append(bookId, null)).withRel("append-sentence"));
        }

        // Sentence vote links are handled by API Gateway or Frontend

        return ResponseEntity.ok().body(ApiResponse.success(bookDetail));
    }

    /**
     * 내가 쓴 문장 목록 조회 API (페이지네이션)
     * GET /api/books/mysentences
     */
    @Operation(summary = "내가 쓴 문장 조회", description = "현재 로그인한 사용자가 작성한 문장 목록을 조회합니다.")
    @GetMapping("/mysentences")
    public ResponseEntity<ApiResponse<SentencePageResponse>> getMySentences(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }

        SentencePageResponse response = bookQueryService.getSentencesByUser(userId, page, size);
        // Sentence vote links are handled by API Gateway or Frontend
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }
}
