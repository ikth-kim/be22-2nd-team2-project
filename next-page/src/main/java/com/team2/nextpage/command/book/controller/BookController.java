package com.team2.nextpage.command.book.controller;

import com.team2.nextpage.command.book.dto.request.CreateBookRequest;
import com.team2.nextpage.command.book.dto.request.SentenceAppendRequest;
import com.team2.nextpage.command.book.service.BookService;
import com.team2.nextpage.common.response.ApiResponse;
import com.team2.nextpage.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * 소설 Command 컨트롤러
 *
 * @author 정진호
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 소설 생성 API
     * POST /api/books
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(@RequestBody @Valid CreateBookRequest request) {
        Long writerId = SecurityUtil.getCurrentUserId();
        Long bookId = bookService.createBook(writerId, request);
        return ResponseEntity.ok(ApiResponse.success(bookId));
    }

    /**
     * 문장 이어쓰기 API
     * POST /api/books/{bookId}/sentences
     */
    @PostMapping("/{bookId}/sentences")
    public ResponseEntity<ApiResponse<Long>> append(@PathVariable Long bookId,
            @RequestBody @Valid SentenceAppendRequest request) {
        Long writerId = SecurityUtil.getCurrentUserId();
        // 만약 request body에 writerId가 있다면 그것을 우선할 수도 있지만, 보안상 토큰에서 가져오는 게 맞음.
        // 현재는 DTO에 writerId 필드가 있는데, 이는 서비스 계층 전달용으로 쓰이거나 클라가 보낼 수도 있음.
        // 여기서는 서비스 메서드가 writerId를 인자로 받으므로, 컨트롤러에서 추출해 넘김.
        // DTO의 writerId는 @NotNull이지만, 클라이언트가 보내는 값보다 토큰 값이 우선이어야 함.
        // 다만 DTO에 writerId가 필수라면 클라이언트가 보내야 함.
        // Request DTO의 writerId 필드를 무시하거나 덮어써야 안전하지만,
        // 편의상 DTO의 writerId를 사용하지 않고 별도 파라미터로 넘김 (BookService가 그렇게 설계됨).

        // 하지만 SentenceAppendRequest에 writerId가 @NotNull로 있음.
        // 즉 클라이언트가 보내야 함.
        // BookService.appendSentence(bookId, writerId, request) 호출 시
        // 서비스에서 request.writerId를 쓸지, 인자 writerId를 쓸지?
        // 서비스 구현: "Sentence.builder()... .writerId(writerId)" -> 인자 writerId 사용.
        // 따라서 request의 writerId는 검증용이거나 무시됨.

        Long sentenceId = bookService.appendSentence(bookId, writerId, request);
        return ResponseEntity.ok(ApiResponse.success(sentenceId));
    }
}
