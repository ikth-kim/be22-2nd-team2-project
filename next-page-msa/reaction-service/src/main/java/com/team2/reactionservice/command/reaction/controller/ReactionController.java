package com.team2.reactionservice.command.reaction.controller;

import com.team2.reactionservice.command.reaction.dto.request.CreateCommentRequest;
import com.team2.reactionservice.command.reaction.dto.request.UpdateCommentRequest;
import com.team2.reactionservice.command.reaction.dto.request.VoteRequest;
import com.team2.reactionservice.command.reaction.service.ReactionService;
import com.team2.commonmodule.response.ApiResponse;
import com.team2.commonmodule.util.SecurityUtil;
import com.team2.reactionservice.websocket.dto.CommentCreatedEvent;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import com.team2.reactionservice.feign.MemberServiceClient;
import java.time.LocalDateTime;

/**
 * 반응(댓글/투표) Command 컨트롤러
 *
 * @author 정병진
 */
@Tag(name = "Reaction Commands", description = "반응(댓글/투표) 관리(Command) API")
@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {

  private final ReactionService reactionService;
  private final SimpMessagingTemplate messagingTemplate;
  private final MemberServiceClient memberServiceClient;

  /**
   * 댓글 등록 API
   * POST /api/reactions/comments
   *
   * @param request 댓글 작성 요청 객체 (Body)
   * @return 생성된 댓글의 ID
   */
  @Operation(summary = "댓글 작성", description = "소설에 새로운 댓글을 작성합니다.")
  @PostMapping("/comments")
  public ResponseEntity<ApiResponse<Long>> createComment(
      @RequestBody @Valid CreateCommentRequest request) {
    Long commentId = reactionService.addComment(request);

    // 실시간 댓글 알림 전송
    Long userId = SecurityUtil.getCurrentUserId();
    String nickname = "User#" + userId;
    try {
      nickname = memberServiceClient.getUserNickname(userId);
    } catch (Exception e) {
      // Fallback or log error
      // log.warn("Failed to fetch nickname for user {}", userId);
    }

    CommentCreatedEvent event = new CommentCreatedEvent(
        commentId,
        request.getBookId(),
        request.getContent(),
        nickname,
        LocalDateTime.now());

    messagingTemplate.convertAndSend("/topic/comments/" + request.getBookId(), event);

    return ResponseEntity.ok(ApiResponse.success(commentId));
  }

  /**
   * 댓글 수정 API
   * PATCH /api/reactions/comments/{commentId}
   *
   * @param commentId 수정할 댓글의 ID
   * @param request   수정할 내용이 담긴 요청 객체(Body)
   * @return 성공시 데이터 없이 성공 응답 반환
   */
  @Operation(summary = "댓글 수정", description = "작성한 댓글을 수정합니다.")
  @PatchMapping("/comments/{commentId}")
  public ResponseEntity<ApiResponse<Void>> modifyComment(
      @PathVariable Long commentId,
      @RequestBody @Valid UpdateCommentRequest request) {
    reactionService.modifyComment(commentId, request);
    return ResponseEntity.ok(ApiResponse.success());
  }

  /**
   * 댓글 삭제 API
   * DELETE /api/reactions/comments/{commentId}
   *
   * @param commentId 삭제할 댓글의 ID (Path Variable)
   * @return 성공 시 데이터 없이 성공 응답 반환
   */
  @Operation(summary = "댓글 삭제", description = "작성한 댓글을 삭제합니다.")
  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<ApiResponse<Void>> removeComment(
      @PathVariable Long commentId) {
    reactionService.removeComment(commentId);
    return ResponseEntity.ok(ApiResponse.success());
  }

  /**
   * 소설 투표 API
   * POST /api/reactions/votes/books
   *
   * @param request 투표 요청 정보 (bookId, voteType)
   * @return 투표 반영 여부 (true: 반영됨 / false: 취소됨)
   */
  @Operation(summary = "소설 투표", description = "소설에 대해 좋아요/싫어요를 투표합니다.")
  @PostMapping("/votes/books")
  public ResponseEntity<ApiResponse<Boolean>> voteBook(
      @RequestBody @Valid VoteRequest request) {
    Boolean result = reactionService.voteBook(request);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  /**
   * 문장 투표 API
   * POST /api/reactions/votes/sentences/{sentenceId}
   *
   * @param sentenceId 투표할 문장 ID
   * @param request    투표 요청 정보 (voteType)
   * @return 투표 반영 여부 (true: 반영됨 / false: 취소됨)
   */
  @Operation(summary = "문장 투표", description = "특정 문장에 대해 좋아요/싫어요를 투표합니다.")
  @PostMapping("/votes/sentences/{sentenceId}")
  public ResponseEntity<ApiResponse<Boolean>> voteSentence(
      @PathVariable Long sentenceId,
      @RequestBody @Valid VoteRequest request) {
    Boolean result = reactionService.voteSentence(sentenceId, request);
    return ResponseEntity.ok(ApiResponse.success(result));
  }
}
