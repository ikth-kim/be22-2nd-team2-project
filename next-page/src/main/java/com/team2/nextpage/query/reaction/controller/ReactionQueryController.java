package com.team2.nextpage.query.reaction.controller;

import com.team2.nextpage.common.response.ApiResponse;
import com.team2.nextpage.query.reaction.dto.response.CommentDto;
import com.team2.nextpage.query.reaction.dto.response.CommentPageResponse;
import com.team2.nextpage.query.reaction.service.ReactionQueryService;
import com.team2.nextpage.command.reaction.controller.ReactionController;
import com.team2.nextpage.common.util.SecurityUtil;
import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * 반응 Query 컨트롤러
 *
 * @author 정병진
 */
@Tag(name = "Reaction Queries", description = "반응(댓글) 조회(Query) API")
@RestController
@RequestMapping("/api/reactions")
public class ReactionQueryController {

  private final ReactionQueryService reactionQueryService;

  public ReactionQueryController(ReactionQueryService reactionQueryService) {
    this.reactionQueryService = reactionQueryService;
  }

  /**
   * 댓글 목록 조회 API
   * GET /api/reactions/comments/{bookId}
   *
   * @param bookId 조회할 소설의 ID
   * @return 댓글 목록 (ApiResponse로 래핑)
   */
  @Operation(summary = "댓글 목록 조회", description = "특정 소설에 달린 댓글 목록을 조회합니다.")
  @GetMapping("/comments/{bookId}")
  public ResponseEntity<ApiResponse<List<CommentDto>>> getComments(@PathVariable Long bookId) {
    List<CommentDto> comments = reactionQueryService.getComments(bookId);
    addLinksToComments(comments);
    return ResponseEntity.ok(ApiResponse.success(comments));
  }

  private void addLinksToComments(List<CommentDto> comments) {
    if (comments == null)
      return;
    for (CommentDto comment : comments) {
      try {
        comment.add(linkTo(methodOn(ReactionController.class).createComment(null)).withRel("reply"));
        comment.add(linkTo(methodOn(ReactionController.class).removeComment(comment.getCommentId())).withRel("delete"));
      } catch (Exception e) {
        // Ignore
      }
      if (comment.getChildren() != null) {
        addLinksToComments(comment.getChildren());
      }
    }
  }

  /**
   * 내가 쓴 댓글 목록 조회 API (페이징)
   * GET /api/reactions/mycomments
   */
  @Operation(summary = "내가 쓴 댓글 조회", description = "현재 로그인한 사용자가 작성한 댓글 목록을 조회합니다.")
  @GetMapping("/mycomments")
  public ResponseEntity<ApiResponse<CommentPageResponse>> getMyComments(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    Long userId = SecurityUtil.getCurrentUserId();
    if (userId == null) {
      throw new BusinessException(ErrorCode.UNAUTHENTICATED);
    }

    return ResponseEntity.ok(ApiResponse.success(reactionQueryService.getCommentsByUser(userId, page, size)));
  }
}
