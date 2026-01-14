package com.team2.reactionservice.command.reaction.service;

import com.team2.reactionservice.command.reaction.dto.request.CreateCommentRequest;
import com.team2.reactionservice.command.reaction.dto.request.UpdateCommentRequest;
import com.team2.reactionservice.command.reaction.dto.request.VoteRequest;
import com.team2.reactionservice.command.reaction.entity.BookVote;
import com.team2.reactionservice.command.reaction.entity.Comment;
import com.team2.reactionservice.command.reaction.entity.SentenceVote;
import com.team2.reactionservice.command.reaction.entity.VoteType;
import com.team2.reactionservice.command.reaction.repository.BookVoteRepository;
import com.team2.reactionservice.command.reaction.repository.CommentRepository;
import com.team2.reactionservice.command.reaction.repository.SentenceVoteRepository;
import com.team2.reactionservice.feign.StoryServiceClient;
import com.team2.reactionservice.websocket.dto.VoteUpdateDto;
import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.commonmodule.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 반응(댓글/투표) Command 서비스
 *
 * @author 정병진
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReactionService {

  private final CommentRepository commentRepository;
  private final BookVoteRepository bookVoteRepository;
  private final SentenceVoteRepository sentenceVoteRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final StoryServiceClient storyServiceClient;

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
        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
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
   */
  public void modifyComment(Long commentId, UpdateCommentRequest request) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

    validateWriter(comment, SecurityUtil.getCurrentUserId());

    comment.updateContent(request.getContent());
  }

  /**
   * 댓글 삭제
   *
   * @param commentId 삭제할 댓글 ID
   */
  public void removeComment(Long commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

    validateWriterOrAdmin(comment, SecurityUtil.getCurrentUserId());

    commentRepository.delete(comment);
  }

  /**
   * 소설 좋아요/싫어요 투표 처리
   */
  public Boolean voteBook(VoteRequest request) {
    Long voterId = SecurityUtil.getCurrentUserId();

    Optional<BookVote> existingVote = bookVoteRepository.findByBookIdAndVoterId(request.getBookId(), voterId);

    if (existingVote.isPresent()) {
      BookVote vote = existingVote.get();
      if (vote.getVoteType() == request.getVoteType()) {
        bookVoteRepository.delete(vote);
        broadcastBookVote(request.getBookId());
        return false;
      } else {
        vote.changeVoteType(request.getVoteType());
        broadcastBookVote(request.getBookId());
        return true;
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

    VoteUpdateDto updateDto = new VoteUpdateDto(
        bookId,
        "BOOK",
        likeCount,
        dislikeCount);

    messagingTemplate.convertAndSend("/topic/books/" + bookId + "/votes", updateDto);
  }

  /**
   * 문장 좋아요/싫어요 투표 처리
   */
  public Boolean voteSentence(Long sentenceId, VoteRequest request) {
    Long voterId = SecurityUtil.getCurrentUserId();

    Optional<SentenceVote> existingVote = sentenceVoteRepository.findBySentenceIdAndVoterId(sentenceId, voterId);

    if (existingVote.isPresent()) {
      SentenceVote vote = existingVote.get();
      if (vote.getVoteType() == request.getVoteType()) {
        sentenceVoteRepository.delete(vote);
        broadcastSentenceVote(sentenceId);
        return false;
      } else {
        vote.changeVoteType(request.getVoteType());
        broadcastSentenceVote(sentenceId);
        return true;
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

    try {
      Long bookId = storyServiceClient.getBookIdBySentenceId(sentenceId);

      if (bookId != null) {
        VoteUpdateDto updateDto = new VoteUpdateDto(
            sentenceId,
            "SENTENCE",
            likeCount,
            dislikeCount);

        messagingTemplate.convertAndSend("/topic/books/" + bookId + "/votes", updateDto);
      }
    } catch (Exception e) {
      log.error("Failed to broadcast sentence vote: {}", e.getMessage());
    }
  }

  private void validateWriter(Comment comment, Long userId) {
    if (!comment.getWriterId().equals(userId)) {
      throw new BusinessException(ErrorCode.NOT_COMMENT_OWNER);
    }
  }

  private void validateWriterOrAdmin(Comment comment, Long userId) {
    if (SecurityUtil.isAdmin()) {
      return;
    }
    if (!comment.getWriterId().equals(userId)) {
      throw new BusinessException(ErrorCode.NOT_COMMENT_OWNER);
    }
  }
}
