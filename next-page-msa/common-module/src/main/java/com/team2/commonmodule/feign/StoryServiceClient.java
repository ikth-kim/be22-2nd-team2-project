package com.team2.commonmodule.feign;

import com.team2.commonmodule.feign.dto.BookBatchInfoDto;
import com.team2.commonmodule.feign.dto.BookInfoDto;
import com.team2.commonmodule.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Story Service Feign Client
 *
 * <p>
 * 다른 MSA 서비스가 story-service의 소설 정보를 조회할 때 사용하는 Feign Client입니다.
 * </p>
 *
 * <p>
 * <b>사용 서비스:</b>
 * <ul>
 *   <li>reaction-service: Comment의 소설 제목 조회</li>
 * </ul>
 * </p>
 *
 * @author MSA Team
 */
@FeignClient(name = "story-service")
public interface StoryServiceClient {

    /**
     * 단일 소설 정보 조회
     *
     * @param bookId 소설 ID
     * @return 소설 정보 (bookId, title, writerId, status)
     */
    @GetMapping("/internal/books/{bookId}")
    ApiResponse<BookInfoDto> getBookInfo(@PathVariable("bookId") Long bookId);

    /**
     * 여러 소설 정보 일괄 조회
     *
     * <p>N+1 문제를 방지하기 위해 여러 소설의 정보를 한 번에 조회합니다.</p>
     *
     * @param bookIds 소설 ID 리스트
     * @return 소설 정보 리스트
     */
    @GetMapping("/internal/books/batch")
    ApiResponse<BookBatchInfoDto> getBooksBatch(@RequestParam("bookIds") List<Long> bookIds);

    /**
     * 소설 존재 여부 확인
     *
     * @param bookId 소설 ID
     * @return 존재 여부
     */
    @GetMapping("/internal/books/{bookId}/exists")
    ApiResponse<Boolean> bookExists(@PathVariable("bookId") Long bookId);
}
