package com.team2.storyservice.query.book.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

/**
 * 소설 목록 페이지 응답 DTO
 *
 * @author 정진호
 */
@Getter
@Setter
@NoArgsConstructor
public class BookPageResponse {
    private List<BookDto> content; // 소설 목록
    private Integer page; // 현재 페이지 번호
    private Integer size; // 페이지 크기
    private Long totalElements; // 전체 요소 수
    private Integer totalPages; // 전체 페이지 수
    private Boolean hasNext; // 다음 페이지 존재 여부
    private Boolean hasPrevious; // 이전 페이지 존재 여부

    public BookPageResponse(List<BookDto> content, Integer page, Integer size, Long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.hasNext = page < totalPages - 1;
        this.hasPrevious = page > 0;
    }
}
