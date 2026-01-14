package com.team2.nextpage.fixtures;

import com.team2.nextpage.auth.dto.LoginRequest;
import com.team2.nextpage.command.book.dto.request.CreateBookRequest;
import com.team2.nextpage.command.book.dto.request.SentenceAppendRequest;
import com.team2.nextpage.command.book.dto.request.UpdateBookRequest;
import com.team2.nextpage.command.book.dto.request.UpdateSentenceRequest;
import com.team2.nextpage.command.member.dto.request.SignUpRequest;
import com.team2.nextpage.command.reaction.dto.request.CreateCommentRequest;
import com.team2.nextpage.command.reaction.dto.request.UpdateCommentRequest;
import com.team2.nextpage.command.reaction.dto.request.VoteRequest;
import com.team2.nextpage.command.reaction.entity.VoteType;

/**
 * Request DTO 테스트 빌더
 * ReflectionTestUtils 사용을 피하고 직접 생성자나 setter를 사용하여 DTO를 생성합니다.
 */
public class RequestDtoTestBuilder {

    /**
     * SignUpRequest 생성
     */
    public static SignUpRequest createSignUpRequest(String email, String password, String nickname) {
        return new SignUpRequest(email, password, nickname);
    }

    /**
     * LoginRequest 생성
     */
    public static LoginRequest createLoginRequest(String email, String password) {
        return new LoginRequest(email, password);
    }

    /**
     * CreateBookRequest 생성
     */
    public static CreateBookRequest createBookRequest(String title, String categoryId,
                                                       String firstSentence, int maxSequence) {
        CreateBookRequest request = new CreateBookRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "title", title);
        org.springframework.test.util.ReflectionTestUtils.setField(request, "categoryId", categoryId);
        org.springframework.test.util.ReflectionTestUtils.setField(request, "firstSentence", firstSentence);
        org.springframework.test.util.ReflectionTestUtils.setField(request, "maxSequence", maxSequence);
        return request;
    }

    /**
     * UpdateBookRequest 생성
     */
    public static UpdateBookRequest updateBookRequest(String title) {
        UpdateBookRequest request = new UpdateBookRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "title", title);
        return request;
    }

    /**
     * SentenceAppendRequest 생성
     */
    public static SentenceAppendRequest sentenceRequest(String content) {
        SentenceAppendRequest request = new SentenceAppendRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "content", content);
        return request;
    }

    /**
     * UpdateSentenceRequest 생성
     */
    public static UpdateSentenceRequest updateSentenceRequest(String content) {
        UpdateSentenceRequest request = new UpdateSentenceRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "content", content);
        return request;
    }

    /**
     * CreateCommentRequest 생성 (루트 댓글)
     */
    public static CreateCommentRequest commentRequest(Long bookId, String content) {
        return CreateCommentRequest.builder()
                .bookId(bookId)
                .content(content)
                .build();
    }

    /**
     * CreateCommentRequest 생성 (대댓글)
     */
    public static CreateCommentRequest commentReplyRequest(Long bookId, String content, Long parentId) {
        return CreateCommentRequest.builder()
                .bookId(bookId)
                .content(content)
                .parentId(parentId)
                .build();
    }

    /**
     * UpdateCommentRequest 생성
     */
    public static UpdateCommentRequest updateCommentRequest(String content) {
        return UpdateCommentRequest.builder()
                .content(content)
                .build();
    }

    /**
     * VoteRequest 생성
     */
    public static VoteRequest voteRequest(Long bookId, VoteType voteType) {
        return VoteRequest.builder()
                .bookId(bookId)
                .voteType(voteType)
                .build();
    }

    /**
     * VoteRequest 생성 (LIKE)
     */
    public static VoteRequest likeRequest(Long bookId) {
        return voteRequest(bookId, VoteType.LIKE);
    }

    /**
     * VoteRequest 생성 (DISLIKE)
     */
    public static VoteRequest dislikeRequest(Long bookId) {
        return voteRequest(bookId, VoteType.DISLIKE);
    }
}
