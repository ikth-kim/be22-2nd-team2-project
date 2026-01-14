package com.team2.storyservice.query.book.service;

import com.team2.commonmodule.feign.MemberServiceClient;
import com.team2.commonmodule.feign.dto.MemberBatchInfoDto;
import com.team2.commonmodule.feign.dto.MemberInfoDto;
import com.team2.commonmodule.response.ApiResponse;
import com.team2.storyservice.query.book.dto.request.BookSearchRequest;
import com.team2.storyservice.query.book.dto.response.BookDetailDto;
import com.team2.storyservice.query.book.dto.response.BookDto;
import com.team2.storyservice.query.book.dto.response.BookPageResponse;
import com.team2.storyservice.query.book.dto.response.SentenceDto;
import com.team2.storyservice.query.book.dto.response.SentencePageResponse;
import com.team2.storyservice.query.book.mapper.BookMapper;
import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 소설 Query 서비스 (조회 전용)
 *
 * @author 정진호
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookQueryService {

    private final BookMapper bookMapper;
    private final MemberServiceClient memberServiceClient;

    /**
     * 소설 검색/목록 조회 (페이징/정렬/필터링)
     *
     * @param request 검색 조건 (페이지, 정렬, 필터)
     * @return 페이징된 소설 목록
     */
    public BookPageResponse searchBooks(BookSearchRequest request) {
        // 검색 조건에 맞는 소설 목록 조회
        List<BookDto> books = bookMapper.findBooks(request);

        // 전체 개수 조회 (페이징 정보용)
        Long totalElements = bookMapper.countBooks(request);

        return new BookPageResponse(books, request.getPage(), request.getSize(), totalElements);
    }

    /**
     * 소설 목록 조회 (기본 - 하위 호환용)
     *
     * @return 전체 소설 목록
     * @deprecated searchBooks(BookSearchRequest) 사용 권장
     */
    @Deprecated
    public List<BookDto> searchBooks() {
        return bookMapper.findAllBooks();
    }

    /**
     * 소설 상세 보기 (기본 정보만)
     *
     * @param bookId 소설 ID
     * @return 소설 기본 정보
     * @throws BusinessException 소설을 찾을 수 없는 경우
     */
    public BookDto getBook(Long bookId) {
        BookDto book = bookMapper.findBookDetail(bookId);
        if (book == null) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }
        return book;
    }

    /**
     * 소설 뷰어 모드 조회 (문장 목록 및 투표 카운트 포함)
     *
     * @param bookId 소설 ID
     * @return 소설 상세 정보 (문장 목록, 투표 카운트 포함)
     * @throws BusinessException 소설을 찾을 수 없는 경우
     */
    public BookDetailDto getBookForViewer(Long bookId) {
        Long userId = com.team2.commonmodule.util.SecurityUtil.getCurrentUserId();

        // 1. 소설 기본 정보 조회
        BookDetailDto book = bookMapper.findBookForViewer(bookId, userId);
        if (book == null) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }

        // 2. 문장 목록 조회
        List<SentenceDto> sentences = bookMapper.findSentencesByBookId(bookId, userId);

        // 3. 투표 카운트 조회
        book.setLikeCount(bookMapper.countLikes(bookId));
        book.setDislikeCount(bookMapper.countDislikes(bookId));

        // 4. MSA: 회원 정보 조회 (Feign Client)
        // 작성자 ID 목록 수집 (중복 제거)
        List<Long> writerIds = sentences.stream()
                .map(SentenceDto::getWriterId)
                .distinct()
                .collect(Collectors.toList());

        // 소설 작성자 ID도 추가
        if (book.getWriterId() != null && !writerIds.contains(book.getWriterId())) {
            writerIds.add(book.getWriterId());
        }

        // 일괄 조회로 N+1 문제 방지
        if (!writerIds.isEmpty()) {
            try {
                ApiResponse<MemberBatchInfoDto> response = memberServiceClient.getMembersBatch(writerIds);
                if (response != null && response.getData() != null) {
                    Map<Long, String> memberMap = response.getData().getMembers().stream()
                            .collect(Collectors.toMap(
                                    MemberInfoDto::getUserId,
                                    MemberInfoDto::getUserNicknm
                            ));

                    // 소설 작성자 닉네임 설정
                    book.setWriterNicknm(memberMap.get(book.getWriterId()));

                    // 각 문장 작성자 닉네임 설정
                    sentences.forEach(sentence ->
                            sentence.setWriterNicknm(memberMap.get(sentence.getWriterId()))
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to fetch member info from member-service: {}", e.getMessage());
                // Feign 호출 실패 시에도 계속 진행 (닉네임은 null로 남음)
            }
        }

        book.setSentences(sentences);
        return book;
    }

    /**
     * 특정 사용자가 작성한 문장 목록 조회 (페이징)
     */
    public SentencePageResponse getSentencesByUser(Long userId, int page, int size) {
        int offset = page * size;
        List<SentenceDto> sentences = bookMapper.findSentencesByWriterId(userId, offset, size);
        Long totalElements = bookMapper.countSentencesByWriterId(userId);

        // MSA: 회원 정보 조회 (Feign Client)
        if (!sentences.isEmpty()) {
            try {
                ApiResponse<MemberInfoDto> response = memberServiceClient.getMemberInfo(userId);
                if (response != null && response.getData() != null) {
                    String nicknm = response.getData().getUserNicknm();
                    sentences.forEach(sentence -> sentence.setWriterNicknm(nicknm));
                }
            } catch (Exception e) {
                log.warn("Failed to fetch member info from member-service: {}", e.getMessage());
            }
        }

        return new SentencePageResponse(sentences, page, size, totalElements);
    }
}
