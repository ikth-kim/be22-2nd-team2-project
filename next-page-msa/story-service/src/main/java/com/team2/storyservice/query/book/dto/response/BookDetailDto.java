package com.team2.storyservice.query.book.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;

/**
 * 소설 뷰어용 상세 DTO (문장 목록 포함)
 *
 * @author 정진호
 */
@Getter
@Setter
@NoArgsConstructor
public class BookDetailDto extends RepresentationModel<BookDetailDto> {
    // 기본 정보
    private Long bookId;
    private Long writerId;
    private String writerNicknm; // 소설 생성자 닉네임
    private String categoryId;
    private String title;
    private String status;
    private Integer currentSequence;
    private Integer maxSequence;
    private Long lastWriterUserId;
    private LocalDateTime createdAt;

    // 문장 목록
    private List<SentenceDto> sentences;

    // 투표 통계
    private Integer likeCount; // 좋아요 수
    private Integer dislikeCount; // 싫어요 수
    private String myVote; // 내 투표 상태 (LIKE/DISLIKE/null)
}
