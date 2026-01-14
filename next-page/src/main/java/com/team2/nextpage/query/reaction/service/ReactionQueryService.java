package com.team2.nextpage.query.reaction.service;

import com.team2.nextpage.query.reaction.dto.response.CommentDto;
import com.team2.nextpage.query.reaction.dto.response.CommentPageResponse;
import com.team2.nextpage.query.reaction.mapper.ReactionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 반응 Query 서비스 (댓글 목록 조회 등)
 *
 * @author 정병진
 */
@Service
@Transactional(readOnly = true)
public class ReactionQueryService {

  private final ReactionMapper reactionMapper;

  public ReactionQueryService(ReactionMapper reactionMapper) {
    this.reactionMapper = reactionMapper;
  }

  /**
   * 댓글 목록 조회
   */

  public List<CommentDto> getComments(Long bookId) {
    // 1. 전체 댓글 조회 (Soft Delete 제외됨)
    List<CommentDto> allComments = reactionMapper.findCommentsByBookId(bookId);

    // 2. Map으로 변환 (Lookup 용도)
    java.util.Map<Long, CommentDto> commentMap = allComments.stream()
        .collect(java.util.stream.Collectors.toMap(CommentDto::getCommentId, dto -> dto));

    // 3. 트리 구조 조립
    java.util.List<CommentDto> roots = new java.util.ArrayList<>();

    for (CommentDto dto : allComments) {
      if (dto.getParentId() == null) {
        roots.add(dto);
      } else {
        CommentDto parent = commentMap.get(dto.getParentId());
        if (parent != null) {
          parent.getChildren().add(dto);
        } else {
          // 부모가 삭제되었거나 찾을 수 없는 경우, 최상위로 노출 (Orphan 처리)
          roots.add(dto);
        }
      }
    }

    // 4. 최상위 댓글만 반환 (자식은 children 필드에 포함됨)
    return roots;
  }

  /**
   * 특정 사용자가 쓴 댓글 목록 조회 (페이징)
   */
  public CommentPageResponse getCommentsByUser(Long userId, int page, int size) {
    int offset = page * size;
    List<CommentDto> comments = reactionMapper.findCommentsByWriterId(userId, offset, size);
    Long totalElements = reactionMapper.countCommentsByWriterId(userId);

    return new CommentPageResponse(comments, page, size, totalElements);
  }
}
