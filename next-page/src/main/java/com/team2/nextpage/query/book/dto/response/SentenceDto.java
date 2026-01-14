package com.team2.nextpage.query.book.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

import org.springframework.hateoas.RepresentationModel;

/**
 * 문장(Sentence) 조회용 DTO
 *
 * @author 정진호
 */
@Getter
@Setter
@NoArgsConstructor
public class SentenceDto extends RepresentationModel<SentenceDto> {
    private Long sentenceId;
    private Integer sequenceNo; // 문장 순서
    private String content; // 문장 내용
    private Long writerId; // 작성자 ID
    private String writerNicknm; // 작성자 닉네임
    private LocalDateTime createdAt; // 작성 시간
    private Integer likeCount; // 좋아요 수
    private Integer dislikeCount; // 싫어요 수
    private String myVote; // 내 투표 상태 (LIKE/DISLIKE/null)
    private Long bookId; // 소설 ID
    private String bookTitle; // 소설 제목
}
