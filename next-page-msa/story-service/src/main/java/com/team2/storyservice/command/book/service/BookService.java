package com.team2.storyservice.command.book.service;

import com.team2.storyservice.command.book.dto.request.CreateBookRequest;
import com.team2.storyservice.command.book.dto.request.SentenceAppendRequest;
import com.team2.storyservice.command.book.entity.Book;
import com.team2.storyservice.command.book.entity.Sentence;
import com.team2.storyservice.command.book.repository.BookRepository;
import com.team2.storyservice.command.book.repository.SentenceRepository;
import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.commonmodule.util.SecurityUtil;
import com.team2.storyservice.websocket.dto.BookCreatedEvent;
import com.team2.storyservice.websocket.dto.SentenceCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.team2.storyservice.command.book.entity.BookStatus;
import com.team2.storyservice.category.repository.CategoryRepository;

/**
 * 소설 Command 서비스 (생성, 문장 작성)
 *
 * @author 정진호
 */
@Service
@Transactional
@RequiredArgsConstructor
public class BookService {

        private final BookRepository bookRepository;
        private final SentenceRepository sentenceRepository;
        private final SimpMessagingTemplate messagingTemplate;
        // Circuit Breaker 적용된 서비스 사용
        private final MemberIntegrationService memberIntegrationService;
        private final CategoryRepository categoryRepository;

        /**
         * 소설 방 생성
         */
        public Long createBook(Long writerId, CreateBookRequest request) {
                // 1. Book 생성
                Book book = Book.builder()
                                .writerId(writerId)
                                .categoryId(request.getCategoryId())
                                .title(request.getTitle())
                                .maxSequence(request.getMaxSequence())
                                .status(BookStatus.WRITING)
                                .currentSequence(1) // 1번 문장부터 시작
                                .build();

                Book savedBook = bookRepository.save(book);

                // 2. 첫 문장 자동 등록
                Sentence firstSentence = Sentence.builder()
                                .book(savedBook)
                                .writerId(writerId)
                                .content(request.getFirstSentence())
                                .sequenceNo(1)
                                .build();

                sentenceRepository.save(firstSentence);

                // 3. 상태 업데이트 (1번 문장 작성 완료 처리 -> 다음은 2번)
                savedBook.updateStateAfterWriting(writerId);

                // 4. WebSocket 이벤트 발행 (새 챕터 생성)
                // Circuit Breaker 적용: 에러 발생 시 "Unknown Writer" 반환
                String writerNickname = memberIntegrationService.getUserNickname(writerId);
                String categoryName = categoryRepository.findById(savedBook.getCategoryId())
                                .map(c -> c.getCategoryName()).orElse("카테고리");

                messagingTemplate.convertAndSend("/topic/books/new",
                                new BookCreatedEvent(
                                                savedBook.getBookId(),
                                                savedBook.getTitle(),
                                                categoryName,
                                                writerNickname));

                return savedBook.getBookId();
        }

        /**
         * 문장 이어 쓰기
         * 관리자는 연속 작성 제한이 적용되지 않습니다.
         */
        public Long appendSentence(Long bookId, Long writerId, SentenceAppendRequest request) {
                // 1. 소설 조회 (비관적 락 적용)
                Book book = bookRepository.findByIdForUpdate(bookId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

                // 2. 작성 가능 여부 검증 (도메인 로직) - 관리자는 연속 작성 허용
                book.validateWritingPossible(writerId, SecurityUtil.isAdmin());

                // 3. 문장 생성
                Sentence sentence = Sentence.builder()
                                .book(book)
                                .writerId(writerId)
                                .content(request.getContent())
                                .sequenceNo(book.getCurrentSequence())
                                .build();

                sentenceRepository.save(sentence);

                // 4. 소설 상태 업데이트 (순서 증가, 마지막 작성자 갱신, 완결 체크)
                book.updateStateAfterWriting(writerId);

                // 5. WebSocket 이벤트 발행 (새 문장 작성)
                String writerNickname = memberIntegrationService.getUserNickname(writerId);
                messagingTemplate.convertAndSend("/topic/sentences/" + bookId,
                                new SentenceCreatedEvent(
                                                bookId,
                                                sentence.getSentenceId(),
                                                sentence.getContent(),
                                                sentence.getSequenceNo(),
                                                writerNickname));

                return sentence.getSentenceId();
        }

        /**
         * 소설 수동 완결 (작성자 전용)
         */
        public void completeBook(Long bookId, Long requesterId) {
                Book book = bookRepository.findById(bookId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

                book.completeManually(requesterId);
        }

        public void updateBookTitle(Long bookId, Long requesterId, String title) {
                Book book = bookRepository.findById(bookId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

                if (!book.getWriterId().equals(requesterId) && !SecurityUtil.isAdmin()) {
                        throw new BusinessException(ErrorCode.NOT_BOOK_OWNER);
                }

                book.updateTitle(title);
        }

        public void updateSentence(Long bookId, Long sentenceId, Long requesterId, String content) {
                Sentence sentence = sentenceRepository.findById(sentenceId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

                if (!sentence.getWriterId().equals(requesterId) && !SecurityUtil.isAdmin()) {
                        throw new BusinessException(ErrorCode.NOT_BOOK_OWNER);
                }

                if (!sentence.getBook().getBookId().equals(bookId)) {
                        throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                }

                // Check if it's the last sentence
                if (sentence.getSequenceNo() != sentence.getBook().getCurrentSequence() - 1) {
                        throw new BusinessException(ErrorCode.SEQUENCE_MISMATCH); // Only last sentence can be edited
                }

                sentence.updateContent(content);
        }

        public void deleteSentence(Long bookId, Long sentenceId, Long requesterId) {
                Sentence sentence = sentenceRepository.findById(sentenceId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

                if (!sentence.getWriterId().equals(requesterId) && !SecurityUtil.isAdmin()) {
                        throw new BusinessException(ErrorCode.NOT_BOOK_OWNER);
                }

                Book book = sentence.getBook();
                if (!book.getBookId().equals(bookId)) {
                        throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                }

                // Only allow deleting the LAST sentence
                if (sentence.getSequenceNo() != book.getCurrentSequence() - 1) {
                        throw new BusinessException(ErrorCode.SEQUENCE_MISMATCH); // Only last sentence can be deleted
                }

                int deletedSequence = sentence.getSequenceNo();
                boolean isLast = (deletedSequence == book.getCurrentSequence() - 1);

                sentenceRepository.delete(sentence);

                if (!isLast) {
                        sentenceRepository.decreaseSequenceAfter(bookId, deletedSequence);
                }

                book.decrementSequence();

                // If deleted sentence was the last one, update lastWriterUserId to the previous
                // one
                if (isLast) {
                        int newLastSeq = deletedSequence - 1;
                        if (newLastSeq > 0) {
                                sentenceRepository.findByBookAndSequenceNo(book, newLastSeq)
                                                .ifPresent(s -> book.updateLastWriterUserId(s.getWriterId()));
                        } else {
                                book.updateLastWriterUserId(null);
                        }
                }
        }

        public void deleteBook(Long bookId, Long requesterId) {
                Book book = bookRepository.findById(bookId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

                if (!book.getWriterId().equals(requesterId) && !SecurityUtil.isAdmin()) {
                        throw new BusinessException(ErrorCode.NOT_BOOK_OWNER);
                }

                bookRepository.delete(book);
        }
}
