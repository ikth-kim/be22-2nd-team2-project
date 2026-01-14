package com.team2.nextpage.fixtures;

import com.team2.nextpage.command.book.entity.Book;
import com.team2.nextpage.command.book.entity.Sentence;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Sentence 엔티티 테스트 빌더
 * 문장 엔티티 생성을 간편하게 지원합니다.
 */
public class SentenceTestBuilder {
    private Long sentenceId = 1L;
    private Book book;
    private Long writerId = 1L;
    private String content = "테스트 문장입니다.";
    private Integer sequenceNo = 1;

    /**
     * 기본 Sentence 빌더 인스턴스 생성
     */
    public static SentenceTestBuilder aSentence() {
        return new SentenceTestBuilder();
    }

    public SentenceTestBuilder withSentenceId(Long sentenceId) {
        this.sentenceId = sentenceId;
        return this;
    }

    public SentenceTestBuilder withBook(Book book) {
        this.book = book;
        return this;
    }

    public SentenceTestBuilder withWriterId(Long writerId) {
        this.writerId = writerId;
        return this;
    }

    public SentenceTestBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public SentenceTestBuilder withSequenceNo(Integer sequenceNo) {
        this.sequenceNo = sequenceNo;
        return this;
    }

    /**
     * Sentence 엔티티 생성
     */
    public Sentence build() {
        Sentence sentence = Sentence.builder()
                .book(book)
                .writerId(writerId)
                .content(content)
                .sequenceNo(sequenceNo)
                .build();

        // ID는 @GeneratedValue이므로 리플렉션으로 설정 (불가피)
        if (sentenceId != null) {
            ReflectionTestUtils.setField(sentence, "sentenceId", sentenceId);
        }

        return sentence;
    }
}
