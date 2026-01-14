package com.team2.nextpage.command.book.entity;

import com.team2.nextpage.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문장(Sentence) 엔티티
 *
 * @author 정진호
 */
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "sentences")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sentence extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sentenceId;

    @Column(name = "writer_id")
    private Long writerId;

    @Column(name = "sequence_no")
    private Integer sequenceNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private String content;

    public void updateContent(String content) {
        this.content = content;
    }
}
