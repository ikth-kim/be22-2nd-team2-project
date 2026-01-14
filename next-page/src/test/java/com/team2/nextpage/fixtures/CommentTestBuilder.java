package com.team2.nextpage.fixtures;

import com.team2.nextpage.command.reaction.entity.Comment;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Comment 엔티티 테스트 빌더
 * 댓글 엔티티 생성을 간편하게 지원하며, 부모-자식 관계도 쉽게 설정할 수 있습니다.
 */
public class CommentTestBuilder {
    private Long commentId = 1L;
    private Long bookId = 1L;
    private Long writerId = 1L;
    private String content = "테스트 댓글입니다.";
    private Long parentId = null;

    /**
     * 기본 Comment 빌더 인스턴스 생성
     */
    public static CommentTestBuilder aComment() {
        return new CommentTestBuilder();
    }

    public CommentTestBuilder withCommentId(Long commentId) {
        this.commentId = commentId;
        return this;
    }

    public CommentTestBuilder withBookId(Long bookId) {
        this.bookId = bookId;
        return this;
    }

    public CommentTestBuilder withWriterId(Long writerId) {
        this.writerId = writerId;
        return this;
    }

    public CommentTestBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * 대댓글로 설정 (부모 댓글 ID 지정)
     */
    public CommentTestBuilder asReplyTo(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    /**
     * Comment 엔티티 생성
     */
    public Comment build() {
        // Comment의 Builder는 parent(Comment) 타입을 받으므로 null 전달
        Comment comment = Comment.builder()
                .bookId(bookId)
                .writerId(writerId)
                .content(content)
                .parent(null)  // parent는 Comment 엔티티 타입 (나중에 필요 시 개선)
                .build();

        // ID는 @GeneratedValue이므로 리플렉션으로 설정 (불가피)
        if (commentId != null) {
            ReflectionTestUtils.setField(comment, "commentId", commentId);
        }

        return comment;
    }
}
