package com.team2.storyservice.command.book.controller;

import com.team2.storyservice.command.book.dto.request.CreateBookRequest;
import com.team2.storyservice.command.book.dto.request.SentenceAppendRequest;
import com.team2.storyservice.command.book.service.BookService;
import com.team2.commonmodule.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.team2.storyservice.command.book.dto.request.UpdateBookRequest;
import com.team2.storyservice.command.book.dto.request.UpdateSentenceRequest;

import jakarta.validation.Valid;

/**
 * 소설 Command 컨트롤러
 *
 * @author 정진호
 */
@Tag(name = "Book Commands", description = "소설 생성 및 관리(Command) API")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

        private final BookService bookService;

        /**
         * 소설 생성 API
         * POST /api/books
         */
        @Operation(summary = "소설 생성", description = "새로운 릴레이 소설을 생성합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "성공"),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청")
        })
        @PostMapping
        public ResponseEntity<com.team2.commonmodule.response.ApiResponse<Long>> create(
                        @RequestBody @Valid CreateBookRequest request) {
                Long writerId = SecurityUtil.getCurrentUserId();
                Long bookId = bookService.createBook(writerId, request);
                return ResponseEntity.ok(com.team2.commonmodule.response.ApiResponse.success(bookId));
        }

        /**
         * 문장 이어쓰기 API
         * POST /api/books/{bookId}/sentences
         */
        @Operation(summary = "문장 이어쓰기", description = "진행 중인 소설에 새로운 문장을 추가합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "성공"),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 (글자수 제한 등)"),
                        @ApiResponse(responseCode = "403", description = "작성 권한 없음 (연속 작성 불가 등)")
        })
        @PostMapping("/{bookId}/sentences")
        public ResponseEntity<com.team2.commonmodule.response.ApiResponse<Long>> append(@PathVariable Long bookId,
                        @RequestBody @Valid SentenceAppendRequest request) {
                Long writerId = SecurityUtil.getCurrentUserId();
                Long sentenceId = bookService.appendSentence(bookId, writerId, request);
                return ResponseEntity.ok(com.team2.commonmodule.response.ApiResponse.success(sentenceId));
        }

        /**
         * 소설 수동 완결 API (작성자 전용)
         * POST /api/books/{bookId}/complete
         */
        @Operation(summary = "소설 완결", description = "진행 중인 소설을 작성자가 수동으로 완결시킵니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "성공"),
                        @ApiResponse(responseCode = "403", description = "권한 없음 (작성자 아님)"),
                        @ApiResponse(responseCode = "400", description = "이미 완결된 소설")
        })
        @PostMapping("/{bookId}/complete")
        public ResponseEntity<com.team2.commonmodule.response.ApiResponse<Void>> complete(@PathVariable Long bookId) {
                Long writerId = SecurityUtil.getCurrentUserId();
                bookService.completeBook(bookId, writerId);
                return ResponseEntity.ok(com.team2.commonmodule.response.ApiResponse.success());
        }

        /**
         * 소설 제목 수정 API (작성자 또는 관리자)
         * PATCH /api/books/{bookId}
         */
        @Operation(summary = "소설 제목 수정", description = "소설의 제목을 수정합니다. 작성자 또는 관리자만 가능합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "성공"),
                        @ApiResponse(responseCode = "403", description = "권한 없음"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 소설")
        })
        @PatchMapping("/{bookId}")
        public ResponseEntity<com.team2.commonmodule.response.ApiResponse<Void>> updateBookTitle(
                        @PathVariable Long bookId,
                        @RequestBody @Valid UpdateBookRequest request) {
                Long requesterId = SecurityUtil.getCurrentUserId();
                bookService.updateBookTitle(bookId, requesterId, request.getTitle());
                return ResponseEntity.ok(com.team2.commonmodule.response.ApiResponse.success());
        }

        /**
         * 문장 내용 수정 API (작성자 또는 관리자)
         * PATCH /api/books/{bookId}/sentences/{sentenceId}
         */
        @Operation(summary = "문장 수정", description = "문장의 내용을 수정합니다. 작성자 또는 관리자만 가능합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "성공"),
                        @ApiResponse(responseCode = "403", description = "권한 없음"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 문장")
        })
        @PatchMapping("/{bookId}/sentences/{sentenceId}")
        public ResponseEntity<com.team2.commonmodule.response.ApiResponse<Void>> updateSentence(
                        @PathVariable Long bookId,
                        @PathVariable Long sentenceId,
                        @RequestBody @Valid UpdateSentenceRequest request) {
                Long requesterId = SecurityUtil.getCurrentUserId();
                bookService.updateSentence(bookId, sentenceId, requesterId, request.getContent());
                return ResponseEntity.ok(com.team2.commonmodule.response.ApiResponse.success());
        }

        /**
         * 소설 삭제 API (작성자 또는 관리자)
         * DELETE /api/books/{bookId}
         */
        @Operation(summary = "소설 삭제", description = "소설을 삭제합니다. 작성자 또는 관리자만 가능합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "성공"),
                        @ApiResponse(responseCode = "403", description = "권한 없음"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 소설")
        })
        @DeleteMapping("/{bookId}")
        public ResponseEntity<com.team2.commonmodule.response.ApiResponse<Void>> deleteBook(@PathVariable Long bookId) {
                Long requesterId = SecurityUtil.getCurrentUserId();
                bookService.deleteBook(bookId, requesterId);
                return ResponseEntity.ok(com.team2.commonmodule.response.ApiResponse.success());
        }

        /**
         * 문장 삭제 API (작성자 또는 관리자)
         * DELETE /api/books/{bookId}/sentences/{sentenceId}
         */
        @Operation(summary = "문장 삭제", description = "문장을 삭제합니다. 삭제 시 순서가 자동 조정됩니다. 작성자 또는 관리자만 가능합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "성공"),
                        @ApiResponse(responseCode = "403", description = "권한 없음"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 문장")
        })
        @DeleteMapping("/{bookId}/sentences/{sentenceId}")
        public ResponseEntity<com.team2.commonmodule.response.ApiResponse<Void>> deleteSentence(
                        @PathVariable Long bookId,
                        @PathVariable Long sentenceId) {
                Long requesterId = SecurityUtil.getCurrentUserId();
                bookService.deleteSentence(bookId, sentenceId, requesterId);
                return ResponseEntity.ok(com.team2.commonmodule.response.ApiResponse.success());
        }
}
