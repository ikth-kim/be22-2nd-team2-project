package com.team2.nextpage.command.reaction.service;

import com.team2.nextpage.command.reaction.dto.request.CreateCommentRequest;
import com.team2.nextpage.command.reaction.dto.request.UpdateCommentRequest;
import com.team2.nextpage.command.reaction.dto.request.VoteRequest;
import com.team2.nextpage.command.reaction.entity.BookVote;
import com.team2.nextpage.command.reaction.entity.Comment;
import com.team2.nextpage.command.reaction.entity.SentenceVote;
import com.team2.nextpage.command.reaction.entity.VoteType;
import com.team2.nextpage.command.reaction.repository.BookVoteRepository;
import com.team2.nextpage.command.reaction.repository.CommentRepository;
import com.team2.nextpage.command.reaction.repository.SentenceVoteRepository;
import com.team2.nextpage.command.book.entity.Sentence;
import com.team2.nextpage.command.book.repository.SentenceRepository;
import com.team2.nextpage.websocket.dto.VoteUpdateDto;
import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;
import com.team2.nextpage.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 반응(댓글/투표) Command 서비스
 *
 * @author 정병진
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ReactionService {

  private final CommentRepository commentRepository;
  private final BookVoteRepository bookVoteRepository;
  private final SentenceVoteRepository sentenceVoteRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final SentenceRepository sentenceRepository;

  /**
   * 댓글 작성
   *
   * @param request 댓글 작성 요청 정보(bookId, content)
   * @return 생성된 댓글의 ID
   */
  public Long addComment(CreateCommentRequest request) {
    Long writerId = SecurityUtil.getCurrentUserId();

    Comment parent = null;
    if (request.getParentId() != null) {
      parent = commentRepository.findById(request.getParentId())
          .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

      // 부모 댓글과 같은 소설인지 검증
      if (!parent.getBookId().equals(request.getBookId())) {
        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE); // 혹은 적절한 에러
      }
    }

    Comment newComment = Comment.builder()
        .bookId(request.getBookId())
        .writerId(writerId)
        .content(request.getContent())
        .parent(parent)
        .build();

    Comment saveComment = commentRepository.save(newComment);

    return saveComment.getCommentId();
  }

  /**
   * 댓글 수정
   *
   * @param commentId 수정할 댓글 ID
   * @param request   수정할 내용이 담긴 DTO
   * @throws BusinessException 댓글이 존재하지 않거나 작성자가 아닌 경우
   */
  public void modifyComment(Long commentId, UpdateCommentRequest request) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

    // 권한 체크 (타인 댓글 수정/삭제 불가 예외 처리)
    validateWriter(comment, SecurityUtil.getCurrentUserId());

    comment.updateContent(request.getContent());
  }

  /**
   * 댓글 삭제
   * 관리자는 모든 댓글을 삭제할 수 있습니다.
   *
   * @param commentId 삭제할 댓글 ID
   * @throws BusinessException 댓글이 존재하지 않거나 권한이 없는 경우
   */
  public void removeComment(Long commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

    // 권한 체크 (관리자는 모든 댓글 삭제 가능)
    validateWriterOrAdmin(comment, SecurityUtil.getCurrentUserId());

    commentRepository.delete(comment);
  }

  /**
   * 소설 좋아요/싫어요 투표 처리 (토글 방식)
   * 1. 기록 없음 -> 투표 생성 (true 반환)
   * 2. 기록 있음 + 같은 타입 -> 투표 취소 (삭제, false 반환)
   * 3. 기록 있음 + 다른 타입 -> 투표 변경 (수정, true 반환)
   *
   * @param request 투표 요청 정보 (bookId, voteType)
   * @return 최종 투표 반영 여부 (true: 반영됨 / false: 취소됨)
   */
  public Boolean voteBook(VoteRequest request) {
    Long voterId = SecurityUtil.getCurrentUserId();

    // 1. 이미 투표한 기록이 있는지 확인
    Optional<BookVote> existingVote = bookVoteRepository.findByBookIdAndVoterId(request.getBookId(), voterId);

    if (existingVote.isPresent()) {
      BookVote vote = existingVote.get();
      // 2. 이미 투표 후 또 누르면 -> 취소
      if (vote.getVoteType() == request.getVoteType()) {
        bookVoteRepository.delete(vote);
        broadcastBookVote(request.getBookId());
        return false;
      } else {
        // 다른 걸 눌렀다면, 다른 걸로 변경(좋아요 -> 싫어요, 싫어요 -> 좋아요)
        vote.changeVoteType(request.getVoteType());
        broadcastBookVote(request.getBookId());
        return true; // 투표 변경
      }
    } else {
      BookVote newVote = BookVote.builder()
          .bookId(request.getBookId())
          .voterId(voterId)
          .voteType(request.getVoteType())
          .build();
      bookVoteRepository.save(newVote);
      broadcastBookVote(request.getBookId());
      return true;
    }
  }

  private void broadcastBookVote(Long bookId) {
    long likeCount = bookVoteRepository.countByBookIdAndVoteType(bookId, VoteType.LIKE);
    long dislikeCount = bookVoteRepository.countByBookIdAndVoteType(bookId, VoteType.DISLIKE);

    VoteUpdateDto updateDto = VoteUpdateDto.builder()
        .targetType("BOOK")
        .targetId(bookId)
        .likeCount(likeCount)
        .dislikeCount(dislikeCount)
        .build();

    messagingTemplate.convertAndSend("/topic/books/" + bookId + "/votes", updateDto);
  }

  /**
   * 문장 좋아요/싫어요 투표 처리 (토글 방식)
   * 1. 기록 없음 -> 투표 생성 (true 반환)
   * 2. 기록 있음 + 같은 타입 -> 투표 취소 (삭제, false 반환)
   * 3. 기록 있음 + 다른 타입 -> 투표 변경 (수정, true 반환)
   *
   * @param sentenceId 투표할 문장 ID
   * @param request    투표 요청 정보 (voteType)
   * @return 최종 투표 반영 여부 (true: 반영됨 / false: 취소됨)
   */
  public Boolean voteSentence(Long sentenceId, VoteRequest request) {
    Long voterId = SecurityUtil.getCurrentUserId();

    // 1. 이미 투표한 기록이 있는지 확인
    Optional<SentenceVote> existingVote = sentenceVoteRepository.findBySentenceIdAndVoterId(sentenceId, voterId);

    if (existingVote.isPresent()) {
      SentenceVote vote = existingVote.get();
      // 2. 이미 투표 후 또 누르면 -> 취소
      if (vote.getVoteType() == request.getVoteType()) {
        sentenceVoteRepository.delete(vote);
        broadcastSentenceVote(sentenceId);
        return false;
      } else {
        // 다른 걸 눌렀다면, 다른 걸로 변경(좋아요 -> 싫어요, 싫어요 -> 좋아요)
        vote.changeVoteType(request.getVoteType());
        broadcastSentenceVote(sentenceId);
        return true; // 투표 변경
      }
    } else {
      SentenceVote newVote = SentenceVote.builder()
          .sentenceId(sentenceId)
          .voterId(voterId)
          .voteType(request.getVoteType())
          .build();
      sentenceVoteRepository.save(newVote);
      broadcastSentenceVote(sentenceId);
      return true;
    }
  }

  private void broadcastSentenceVote(Long sentenceId) {
    long likeCount = sentenceVoteRepository.countBySentenceIdAndVoteType(sentenceId, VoteType.LIKE);
    long dislikeCount = sentenceVoteRepository.countBySentenceIdAndVoteType(sentenceId, VoteType.DISLIKE);

    // Find bookId to determine the topic
    Long bookId = sentenceRepository.findById(sentenceId)
        .map(sentence -> sentence.getBook().getBookId())
        .orElse(null);

    if (bookId != null) {
      VoteUpdateDto updateDto = VoteUpdateDto.builder()
          .targetType("SENTENCE")
          .targetId(sentenceId)
          .likeCount(likeCount)
          .dislikeCount(dislikeCount)
          .build();

      messagingTemplate.convertAndSend("/topic/books/" + bookId + "/votes", updateDto);
    }
  }

  /**
   * 작성자 권한 검증 (수정 전용)
   * 요청한 사용자(userId)가 댓글 작성자(writerId)와 일치하는지 확인합니다.
   * 수정은 작성자 본인만 가능합니다.
   *
   * @param comment 검증할 댓글 엔티티
   * @param userId  요청을 보낸 사용자의 ID
   * @throws BusinessException 작성자가 아닐 경우 예외 발생
   */
  private void validateWriter(Comment comment, Long userId) {
    if (!comment.getWriterId().equals(userId)) {
      throw new BusinessException(ErrorCode.NOT_COMMENT_OWNER);
    }
  }

  /**
   * 작성자 또는 관리자 권한 검증 (삭제 전용)
   * 관리자는 모든 댓글을 삭제할 수 있습니다.
   *
   * @param comment 검증할 댓글 엔티티
   * @param userId  요청을 보낸 사용자의 ID
   * @throws BusinessException 작성자도 아니고 관리자도 아닐 경우 예외 발생
   */
  private void validateWriterOrAdmin(Comment comment, Long userId) {
    // 관리자는 모든 댓글 삭제 가능
    if (SecurityUtil.isAdmin()) {
      return;
    }
    // 일반 사용자는 본인 댓글만 삭제 가능
    if (!comment.getWriterId().equals(userId)) {
      throw new BusinessException(ErrorCode.NOT_COMMENT_OWNER);
    }
  }
}
