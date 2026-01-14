package com.team2.nextpage.command.book.service;

import com.team2.nextpage.category.entity.Category;
import com.team2.nextpage.category.repository.CategoryRepository;
import com.team2.nextpage.command.book.dto.request.CreateBookRequest;
import com.team2.nextpage.command.book.dto.request.SentenceAppendRequest;
import com.team2.nextpage.command.book.entity.Book;
import com.team2.nextpage.command.book.entity.BookStatus;
import com.team2.nextpage.command.book.entity.Sentence;
import com.team2.nextpage.command.book.repository.BookRepository;
import com.team2.nextpage.command.book.repository.SentenceRepository;
import com.team2.nextpage.command.member.entity.Member;
import com.team2.nextpage.command.member.repository.MemberRepository;
import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;
import com.team2.nextpage.common.util.SecurityUtil;
import com.team2.nextpage.fixtures.BookTestBuilder;
import com.team2.nextpage.fixtures.MemberTestBuilder;
import com.team2.nextpage.fixtures.RequestDtoTestBuilder;
import com.team2.nextpage.fixtures.SentenceTestBuilder;
import com.team2.nextpage.websocket.dto.SentenceCreatedEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;
    @Mock
    private SentenceRepository sentenceRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private CategoryRepository categoryRepository;

    private static MockedStatic<SecurityUtil> securityUtil;

    @BeforeAll
    public static void beforeAll() {
        securityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterAll
    public static void afterAll() {
        securityUtil.close();
    }

    @Test
    @DisplayName("소설 생성 성공")
    void createBook_Success() {
        // given
        Long writerId = 1L;
        CreateBookRequest request = RequestDtoTestBuilder.createBookRequest(
                "New Novel", "1", "Start", 10
        );

        Book savedBook = BookTestBuilder.aBook()
                .withBookId(10L)
                .withWriterId(writerId)
                .withCategory("1")
                .withTitle("New Novel")
                .build();

        Member writer = MemberTestBuilder.aMember()
                .withUserId(writerId)
                .withNickname("Writer")
                .build();

        Category category = new Category("1", "Fantasy");

        given(bookRepository.save(any(Book.class))).willReturn(savedBook);
        given(memberRepository.findById(writerId)).willReturn(Optional.of(writer));
        given(categoryRepository.findById(any())).willReturn(Optional.of(category));

        // when
        Long bookId = bookService.createBook(writerId, request);

        // then
        assertThat(bookId).isEqualTo(10L);
        verify(sentenceRepository, times(1)).save(any(Sentence.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("문장 이어쓰기 성공")
    void appendSentence_Success() {
        // given
        Long bookId = 10L;
        Long writerId = 2L;
        SentenceAppendRequest request = RequestDtoTestBuilder.sentenceRequest("Next sentence");

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .atSequence(2)
                .withMaxSequence(10)
                .withLastWriter(1L) // 이전 작성자 다름
                .build();

        Member writer = MemberTestBuilder.aMember()
                .withUserId(writerId)
                .withNickname("Writer2")
                .build();

        given(bookRepository.findByIdForUpdate(bookId)).willReturn(Optional.of(book));
        given(SecurityUtil.isAdmin()).willReturn(false);
        given(memberRepository.findById(writerId)).willReturn(Optional.of(writer));

        // when
        bookService.appendSentence(bookId, writerId, request);

        // then
        verify(sentenceRepository, times(1)).save(any(Sentence.class));
        assertThat(book.getCurrentSequence()).isEqualTo(3);
        assertThat(book.getLastWriterUserId()).isEqualTo(writerId);
    }

    @Test
    @DisplayName("문장 이어쓰기 실패 - 연속 작성")
    void appendSentence_Fail_ConsecutiveWriting() {
        // given
        Long bookId = 10L;
        Long writerId = 1L; // 같은 작성자
        SentenceAppendRequest request = RequestDtoTestBuilder.sentenceRequest("Another sentence");

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .atSequence(2)
                .withLastWriter(writerId)
                .build();

        given(bookRepository.findByIdForUpdate(bookId)).willReturn(Optional.of(book));
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when & then
        assertThatThrownBy(() -> bookService.appendSentence(bookId, writerId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONSECUTIVE_WRITING_NOT_ALLOWED);
    }

    @Test
    @DisplayName("문장 이어쓰기 성공 - 관리자 연속 작성 허용")
    void appendSentence_Success_AdminBypass() {
        // given
        Long bookId = 10L;
        Long writerId = 1L; // 같은 작성자 (관리자)
        SentenceAppendRequest request = RequestDtoTestBuilder.sentenceRequest("Admin sentence");

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .atSequence(2)
                .withMaxSequence(10)
                .withLastWriter(writerId)
                .build();

        Member admin = MemberTestBuilder.aMember()
                .withUserId(writerId)
                .withNickname("Admin")
                .asAdmin()
                .build();

        given(bookRepository.findByIdForUpdate(bookId)).willReturn(Optional.of(book));
        given(SecurityUtil.isAdmin()).willReturn(true);
        given(memberRepository.findById(writerId)).willReturn(Optional.of(admin));

        // when
        bookService.appendSentence(bookId, writerId, request);

        // then
        verify(sentenceRepository, times(1)).save(any(Sentence.class));
        assertThat(book.getCurrentSequence()).isEqualTo(3);
    }

    @Test
    @DisplayName("문장 이어쓰기 실패 - 완결된 소설")
    void appendSentence_Fail_AlreadyCompleted() {
        // given
        Long bookId = 10L;
        Long writerId = 2L;
        SentenceAppendRequest request = RequestDtoTestBuilder.sentenceRequest("Another sentence");

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .completed()
                .build();

        given(bookRepository.findByIdForUpdate(bookId)).willReturn(Optional.of(book));
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when & then
        assertThatThrownBy(() -> bookService.appendSentence(bookId, writerId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_COMPLETED);
    }

    @Test
    @DisplayName("문장 이어쓰기 - 최대 시퀀스 도달 시 자동 완결")
    void appendSentence_AutoComplete_WhenMaxSequenceReached() {
        // given
        Long bookId = 10L;
        Long writerId = 2L;
        SentenceAppendRequest request = RequestDtoTestBuilder.sentenceRequest("Final sentence");

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .atSequence(10)  // maxSequence와 동일
                .withMaxSequence(10)
                .withLastWriter(1L)
                .build();

        Member writer = MemberTestBuilder.aMember()
                .withUserId(writerId)
                .withNickname("Writer2")
                .build();

        given(bookRepository.findByIdForUpdate(bookId)).willReturn(Optional.of(book));
        given(SecurityUtil.isAdmin()).willReturn(false);
        given(memberRepository.findById(writerId)).willReturn(Optional.of(writer));

        // when
        bookService.appendSentence(bookId, writerId, request);

        // then
        verify(sentenceRepository, times(1)).save(any(Sentence.class));
        assertThat(book.getCurrentSequence()).isEqualTo(11);
        assertThat(book.getStatus()).isEqualTo(BookStatus.COMPLETED);
    }

    @Test
    @DisplayName("문장 이어쓰기 - WebSocket 이벤트 발행 확인")
    void appendSentence_PublishesWebSocketEvent() {
        // given
        Long bookId = 10L;
        Long writerId = 2L;
        SentenceAppendRequest request = RequestDtoTestBuilder.sentenceRequest("Test sentence");

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .atSequence(2)
                .withMaxSequence(10)
                .withLastWriter(1L)
                .build();

        Member writer = MemberTestBuilder.aMember()
                .withUserId(writerId)
                .withNickname("TestWriter")
                .build();

        given(bookRepository.findByIdForUpdate(bookId)).willReturn(Optional.of(book));
        given(SecurityUtil.isAdmin()).willReturn(false);
        given(memberRepository.findById(writerId)).willReturn(Optional.of(writer));

        // when
        bookService.appendSentence(bookId, writerId, request);

        // then
        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/sentences/" + bookId),
                any(SentenceCreatedEvent.class)
        );
    }

    @Test
    @DisplayName("소설 수동 완결 성공 - 작성자")
    void completeBook_Success_ByWriter() {
        // given
        Long bookId = 10L;
        Long writerId = 1L;

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .withWriterId(writerId)
                .atSequence(5)
                .build();

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));

        // when
        bookService.completeBook(bookId, writerId);

        // then
        assertThat(book.getStatus()).isEqualTo(BookStatus.COMPLETED);
    }

    @Test
    @DisplayName("소설 수동 완결 실패 - 권한 없음")
    void completeBook_Fail_NotWriter() {
        // given
        Long bookId = 10L;
        Long writerId = 1L;
        Long otherId = 2L;

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .withWriterId(writerId)
                .build();

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));

        // when & then
        assertThatThrownBy(() -> bookService.completeBook(bookId, otherId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_BOOK_OWNER);
    }

    @Test
    @DisplayName("소설 제목 수정 성공 - 작성자")
    void updateBookTitle_Success_ByWriter() {
        // given
        Long bookId = 10L;
        Long writerId = 1L;
        String newTitle = "Updated Title";

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .withWriterId(writerId)
                .withTitle("Old Title")
                .build();

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when
        bookService.updateBookTitle(bookId, writerId, newTitle);

        // then
        assertThat(book.getTitle()).isEqualTo(newTitle);
    }

    @Test
    @DisplayName("소설 제목 수정 성공 - 관리자")
    void updateBookTitle_Success_ByAdmin() {
        // given
        Long bookId = 10L;
        Long writerId = 1L;
        Long adminId = 2L;
        String newTitle = "Admin Updated Title";

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .withWriterId(writerId)
                .withTitle("Old Title")
                .build();

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(SecurityUtil.isAdmin()).willReturn(true);

        // when
        bookService.updateBookTitle(bookId, adminId, newTitle);

        // then
        assertThat(book.getTitle()).isEqualTo(newTitle);
    }

    @Test
    @DisplayName("소설 제목 수정 실패 - 권한 없음")
    void updateBookTitle_Fail_Unauthorized() {
        // given
        Long bookId = 10L;
        Long writerId = 1L;
        Long otherId = 2L;

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .withWriterId(writerId)
                .build();

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when & then
        assertThatThrownBy(() -> bookService.updateBookTitle(bookId, otherId, "New Title"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_BOOK_OWNER);
    }

    @Test
    @DisplayName("문장 수정 성공 - 작성자 본인")
    void updateSentence_Success_ByWriter() {
        // given
        Long bookId = 10L;
        Long sentenceId = 1L;
        Long writerId = 1L;
        String newContent = "Updated content";

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .atSequence(2)  // currentSequence = 2
                .build();

        Sentence sentence = SentenceTestBuilder.aSentence()
                .withSentenceId(sentenceId)
                .withBook(book)
                .withWriterId(writerId)
                .withSequenceNo(1)  // 마지막 문장 (currentSequence - 1 = 1)
                .withContent("Old content")
                .build();

        given(sentenceRepository.findById(sentenceId)).willReturn(Optional.of(sentence));
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when
        bookService.updateSentence(bookId, sentenceId, writerId, newContent);

        // then
        assertThat(sentence.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("문장 수정 실패 - 마지막 문장이 아님")
    void updateSentence_Fail_NotLastSentence() {
        // given
        Long bookId = 10L;
        Long sentenceId = 1L;
        Long writerId = 1L;

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .atSequence(5)  // currentSequence = 5
                .build();

        Sentence sentence = SentenceTestBuilder.aSentence()
                .withSentenceId(sentenceId)
                .withBook(book)
                .withWriterId(writerId)
                .withSequenceNo(2)  // 마지막이 아님 (currentSequence - 1 = 4)
                .build();

        given(sentenceRepository.findById(sentenceId)).willReturn(Optional.of(sentence));
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when & then
        assertThatThrownBy(() -> bookService.updateSentence(bookId, sentenceId, writerId, "New content"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEQUENCE_MISMATCH);
    }

    @Test
    @DisplayName("문장 수정 실패 - 권한 없음")
    void updateSentence_Fail_Unauthorized() {
        // given
        Long bookId = 10L;
        Long sentenceId = 1L;
        Long writerId = 1L;
        Long otherId = 2L;

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .atSequence(2)
                .build();

        Sentence sentence = SentenceTestBuilder.aSentence()
                .withSentenceId(sentenceId)
                .withBook(book)
                .withWriterId(writerId)
                .withSequenceNo(1)
                .build();

        given(sentenceRepository.findById(sentenceId)).willReturn(Optional.of(sentence));
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when & then
        assertThatThrownBy(() -> bookService.updateSentence(bookId, sentenceId, otherId, "New content"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_BOOK_OWNER);
    }

    @Test
    @DisplayName("문장 삭제 성공 - 마지막 문장")
    void deleteSentence_Success_LastSentence() {
        // given
        Long bookId = 10L;
        Long sentenceId = 1L;
        Long writerId = 1L;

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .atSequence(3)  // currentSequence = 3
                .build();

        Sentence sentence = SentenceTestBuilder.aSentence()
                .withSentenceId(sentenceId)
                .withBook(book)
                .withWriterId(writerId)
                .withSequenceNo(2)  // 마지막 문장 (currentSequence - 1 = 2)
                .build();

        given(sentenceRepository.findById(sentenceId)).willReturn(Optional.of(sentence));
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when
        bookService.deleteSentence(bookId, sentenceId, writerId);

        // then
        verify(sentenceRepository, times(1)).delete(sentence);
        assertThat(book.getCurrentSequence()).isEqualTo(2);
    }

    @Test
    @DisplayName("문장 삭제 실패 - 마지막 문장이 아님")
    void deleteSentence_Fail_NotLastSentence() {
        // given
        Long bookId = 10L;
        Long sentenceId = 1L;
        Long writerId = 1L;

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .atSequence(5)
                .build();

        Sentence sentence = SentenceTestBuilder.aSentence()
                .withSentenceId(sentenceId)
                .withBook(book)
                .withWriterId(writerId)
                .withSequenceNo(2)  // 마지막이 아님
                .build();

        given(sentenceRepository.findById(sentenceId)).willReturn(Optional.of(sentence));
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when & then
        assertThatThrownBy(() -> bookService.deleteSentence(bookId, sentenceId, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEQUENCE_MISMATCH);
    }

    @Test
    @DisplayName("문장 삭제 - lastWriterUserId 이전 작성자로 갱신")
    void deleteSentence_UpdatesLastWriterUserId() {
        // given
        Long bookId = 10L;
        Long sentenceId = 2L;
        Long currentWriterId = 2L;
        Long previousWriterId = 1L;

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .atSequence(3)
                .withLastWriter(currentWriterId)
                .build();

        Sentence lastSentence = SentenceTestBuilder.aSentence()
                .withSentenceId(sentenceId)
                .withBook(book)
                .withWriterId(currentWriterId)
                .withSequenceNo(2)
                .build();

        Sentence previousSentence = SentenceTestBuilder.aSentence()
                .withSentenceId(1L)
                .withBook(book)
                .withWriterId(previousWriterId)
                .withSequenceNo(1)
                .build();

        given(sentenceRepository.findById(sentenceId)).willReturn(Optional.of(lastSentence));
        given(SecurityUtil.isAdmin()).willReturn(false);
        given(sentenceRepository.findByBookAndSequenceNo(book, 1))
                .willReturn(Optional.of(previousSentence));

        // when
        bookService.deleteSentence(bookId, sentenceId, currentWriterId);

        // then
        verify(sentenceRepository).delete(lastSentence);
        assertThat(book.getLastWriterUserId()).isEqualTo(previousWriterId);
    }

    @Test
    @DisplayName("소설 삭제 성공 - 작성자")
    void deleteBook_Success_ByWriter() {
        // given
        Long bookId = 10L;
        Long writerId = 1L;

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .withWriterId(writerId)
                .build();

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when
        bookService.deleteBook(bookId, writerId);

        // then
        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    @DisplayName("소설 삭제 실패 - 권한 없음")
    void deleteBook_Fail_Unauthorized() {
        // given
        Long bookId = 10L;
        Long writerId = 1L;
        Long otherId = 2L;

        Book book = BookTestBuilder.aBook()
                .withBookId(bookId)
                .withWriterId(writerId)
                .build();

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when & then
        assertThatThrownBy(() -> bookService.deleteBook(bookId, otherId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_BOOK_OWNER);
    }
}
