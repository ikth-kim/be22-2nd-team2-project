package com.team2.nextpage.fixtures;

import com.team2.nextpage.command.book.entity.Book;
import com.team2.nextpage.command.book.entity.BookStatus;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Book 엔티티 테스트 빌더
 * 소설 엔티티의 다양한 상태를 쉽게 생성할 수 있도록 지원합니다.
 */
public class BookTestBuilder {
    private Long bookId = 1L;
    private Long writerId = 1L;
    private String categoryId = "ROMANCE";
    private String title = "테스트 소설";
    private BookStatus status = BookStatus.WRITING;
    private Integer currentSequence = 1;
    private Integer maxSequence = 20;
    private Long lastWriterUserId = null;

    /**
     * 기본 Book 빌더 인스턴스 생성
     */
    public static BookTestBuilder aBook() {
        return new BookTestBuilder();
    }

    public BookTestBuilder withBookId(Long bookId) {
        this.bookId = bookId;
        return this;
    }

    public BookTestBuilder withWriterId(Long writerId) {
        this.writerId = writerId;
        return this;
    }

    public BookTestBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public BookTestBuilder withCategory(String categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    /**
     * 완결된 소설로 설정
     */
    public BookTestBuilder completed() {
        this.status = BookStatus.COMPLETED;
        return this;
    }

    /**
     * 현재 시퀀스 설정
     */
    public BookTestBuilder atSequence(int sequence) {
        this.currentSequence = sequence;
        return this;
    }

    /**
     * 최대 시퀀스 설정
     */
    public BookTestBuilder withMaxSequence(int maxSequence) {
        this.maxSequence = maxSequence;
        return this;
    }

    /**
     * 마지막 작성자 설정
     */
    public BookTestBuilder withLastWriter(Long lastWriterUserId) {
        this.lastWriterUserId = lastWriterUserId;
        return this;
    }

    /**
     * Book 엔티티 생성
     */
    public Book build() {
        Book book = Book.builder()
                .writerId(writerId)
                .categoryId(categoryId)
                .title(title)
                .status(status)
                .currentSequence(currentSequence)
                .maxSequence(maxSequence)
                .lastWriterUserId(lastWriterUserId)
                .build();

        // ID는 @GeneratedValue이므로 리플렉션으로 설정 (불가피)
        if (bookId != null) {
            ReflectionTestUtils.setField(book, "bookId", bookId);
        }

        return book;
    }
}
