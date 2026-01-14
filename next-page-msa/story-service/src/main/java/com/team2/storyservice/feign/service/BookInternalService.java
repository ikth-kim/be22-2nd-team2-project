package com.team2.storyservice.feign.service;

import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.commonmodule.feign.dto.BookBatchInfoDto;
import com.team2.commonmodule.feign.dto.BookInfoDto;
import com.team2.storyservice.command.book.entity.Book;
import com.team2.storyservice.command.book.repository.BookRepository;
import com.team2.storyservice.command.book.repository.SentenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 내부 MSA 서비스 간 통신용 서비스 (소설)
 *
 * <p>
 * 다른 MSA 서비스가 소설 정보를 조회할 때 사용하는 서비스입니다.
 * </p>
 *
 * @author 정진호
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookInternalService {

    private final BookRepository bookRepository;
    private final SentenceRepository sentenceRepository;

    /**
     * 단일 소설 정보 조회
     *
     * @param bookId 소설 ID
     * @return 소설 정보
     * @throws BusinessException 소설을 찾을 수 없는 경우
     */
    public BookInfoDto getBookInfo(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        return new BookInfoDto(
                book.getBookId(),
                book.getTitle(),
                book.getWriterId(),
                book.getStatus().name());
    }

    /**
     * 여러 소설 정보 일괄 조회
     *
     * @param bookIds 소설 ID 리스트
     * @return 소설 정보 리스트
     */
    public BookBatchInfoDto getBooksBatch(List<Long> bookIds) {
        List<Book> books = bookRepository.findAllById(bookIds);

        List<BookInfoDto> bookInfoList = books.stream()
                .map(book -> new BookInfoDto(
                        book.getBookId(),
                        book.getTitle(),
                        book.getWriterId(),
                        book.getStatus().name()))
                .collect(Collectors.toList());

        return new BookBatchInfoDto(bookInfoList);
    }

    /**
     * 소설 존재 여부 확인
     *
     * @param bookId 소설 ID
     * @return 존재 여부
     */
    public boolean bookExists(Long bookId) {
        return bookRepository.existsById(bookId);
    }

    /**
     * 문장 ID로 해당 책의 ID 조회
     *
     * @param sentenceId 문장 ID
     * @return 책 ID
     * @throws BusinessException 문장을 찾을 수 없는 경우
     */
    public Long getBookIdBySentenceId(Long sentenceId) {
        return sentenceRepository.findById(sentenceId)
                .map(sentence -> sentence.getBook().getBookId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
    }
}
