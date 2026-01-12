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
        Long sentenceId = bookService.appendSentence(bookId, writerId, request);
        return ResponseEntity.ok(ApiResponse.success(sentenceId));
    }
}
