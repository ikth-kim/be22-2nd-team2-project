package com.team2.storyservice.feign.controller;

import com.team2.commonmodule.feign.dto.BookBatchInfoDto;
import com.team2.commonmodule.feign.dto.BookInfoDto;
import com.team2.commonmodule.response.ApiResponse;
import com.team2.storyservice.feign.service.BookInternalService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 내부 MSA 서비스 간 통신용 컨트롤러 (소설)
 *
 * <p>
 * 이 컨트롤러는 다른 MSA 서비스(reaction-service)가
 * Feign Client를 통해 소설 정보를 조회할 때 사용됩니다.
 * </p>
 *
 * <p>
 * <b>주의:</b> 이 API는 Gateway를 거치지 않고 내부 네트워크에서만 접근 가능해야 합니다.
 * </p>
 *
 * @author 정진호
 */
@Hidden
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class BookInternalController {

    private final BookInternalService bookInternalService;

    /**
     * 단일 소설 정보 조회 (Feign Client용)
     *
     * @param bookId 조회할 소설 ID
     * @return 소설 정보 (bookId, title, writerId, status)
     */
    @GetMapping("/books/{bookId}")
    public ResponseEntity<ApiResponse<BookInfoDto>> getBookInfo(@PathVariable Long bookId) {
        BookInfoDto bookInfo = bookInternalService.getBookInfo(bookId);
        return ResponseEntity.ok(ApiResponse.success(bookInfo));
    }

    /**
     * 여러 소설 정보 일괄 조회 (Feign Client용)
     *
     * @param bookIds 조회할 소설 ID 리스트
     * @return 소설 정보 리스트
     */
    @GetMapping("/books/batch")
    public ResponseEntity<ApiResponse<BookBatchInfoDto>> getBooksBatch(@RequestParam List<Long> bookIds) {
        BookBatchInfoDto books = bookInternalService.getBooksBatch(bookIds);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    /**
     * 소설 존재 여부 확인 (Feign Client용)
     *
     * @param bookId 확인할 소설 ID
     * @return 존재 여부
     */
    @GetMapping("/books/{bookId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> bookExists(@PathVariable Long bookId) {
        boolean exists = bookInternalService.bookExists(bookId);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    /**
     * 문장 ID로 해당 책의 ID 조회 (Feign Client용)
     *
     * @param sentenceId 문장 ID
     * @return 책 ID
     */
    @GetMapping("/sentences/{sentenceId}/book-id")
    public ResponseEntity<Long> getBookIdBySentenceId(@PathVariable Long sentenceId) {
        Long bookId = bookInternalService.getBookIdBySentenceId(sentenceId);
        return ResponseEntity.ok(bookId);
    }
}
