package com.team2.reactionservice.query.reaction.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

import org.springframework.hateoas.RepresentationModel;

/**
 * 댓글 조회용 DTO
 *
 * @author 정병진
 */
@Getter
@Setter
@NoArgsConstructor
public class CommentDto extends RepresentationModel<CommentDto> {
    private Long commentId;
    private String content;
    private Long writerId; // MSA: Feign Client로 조회하기 위한 ID
    private String writerNicknm;
    private LocalDateTime createdAt;

    // 대댓글 지원
    private Long parentId;
    private java.util.List<CommentDto> children = new java.util.ArrayList<>();

    // 마이페이지용 추가 정보
    private Long bookId;
    private String bookTitle;
}
