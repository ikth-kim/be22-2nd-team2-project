package com.team2.nextpage.command.reaction.controller;

import com.team2.nextpage.command.reaction.dto.request.CreateCommentRequest;
import com.team2.nextpage.command.reaction.dto.request.UpdateCommentRequest;
import com.team2.nextpage.command.reaction.service.ReactionService;
import com.team2.nextpage.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 반응(댓글/투표) Command 컨트롤러
 *
 * @author 정병진
 */
@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {

  private final ReactionService reactionService;

  /**
   * 댓글 등록 API
   * POST /api/reactions/comments
   *
   * @param request 댓글 작성 요청 객체 (Body)
   * @return        생성된 댓글의 ID
   */
  @PostMapping("/comments")
  public ResponseEntity<ApiResponse<Long>> createComment(
      @RequestBody CreateCommentRequest request) {

    Long commentId = reactionService.addComment(request);
    return ResponseEntity.ok(ApiResponse.success(commentId));
  }

  /**
   * 댓글 수정 API
   * PATCH /api/reactions/comments/{commentId}
   *
   * @param commentId 수정할 댓글의 ID
   * @param request   수정할 내용이 담긴 요청 객체(Body)
   * @return          성공시 데이터 없이 성공 응답 반환
   */
  @PatchMapping("/comments/{commentId}")
  public ResponseEntity<ApiResponse<Void>> modifyComment(
      @PathVariable Long commentId,
      @RequestBody UpdateCommentRequest request
  ) {
    reactionService.modifyComment(commentId, request);
    return ResponseEntity.ok(ApiResponse.success());


  }

  /**
   * 댓글 삭제 API
   * DELETE /api/reactions/comments/{commentId}
   *
   * @param commentId 삭제할 댓글의 ID (Path Variable)
   * @return          성공 시 데이터 없이 성공 응답 반환
   */
  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<ApiResponse<Void>> removeComment(
      @PathVariable Long commentId
  ) {
    reactionService.removeComment(commentId);
    return ResponseEntity.ok(ApiResponse.success());
  }

  /**
   * 투표 API
   * POST /api/reactions/votes
   */
  @PostMapping("/votes")
  public ResponseEntity<ApiResponse<Boolean>> vote(/* @RequestBody VoteRequest request */) {
    Boolean result = reactionService.voteBook(/* request */);
    return ResponseEntity.ok(ApiResponse.success(result));
  }
}
