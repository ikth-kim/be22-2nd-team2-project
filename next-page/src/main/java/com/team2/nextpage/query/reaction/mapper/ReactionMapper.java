package com.team2.nextpage.query.reaction.mapper;

import com.team2.nextpage.query.reaction.dto.response.CommentDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 반응(댓글/투표) Query Mapper
 *
 * @author 정병진
 */
@Mapper
public interface ReactionMapper {
    /**
     * 특정 소설의 댓글 목록 조회
     */
    List<CommentDto> findCommentsByBookId(Long bookId);

    /**
     * 특정 사용자가 쓴 댓글 목록 조회 (페이징)
     */
    List<CommentDto> findCommentsByWriterId(Long writerId, int offset, int limit);

    /**
     * 특정 사용자가 쓴 댓글 전체 개수
     */
    Long countCommentsByWriterId(Long writerId);
}
