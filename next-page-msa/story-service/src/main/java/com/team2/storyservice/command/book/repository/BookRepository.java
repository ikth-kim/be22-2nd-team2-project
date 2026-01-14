package com.team2.storyservice.command.book.repository;

import com.team2.storyservice.command.book.entity.Book;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 소설 Command Repository
 *
 * @author 정진호
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * 문장 작성 시 동시성 제어를 위해 비관적 락(X-Lock)을 걸고 조회합니다.
     * 다른 트랜잭션에서 소설에 접근하지 못하도록 막습니다.
     *
     * @param bookId 조회할 소설 ID
     * @return 소설 엔티티 (Lock 걸림)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "3000") }) // 3초 타임아웃
    @Query("SELECT b FROM Book b WHERE b.bookId = :bookId")
    Optional<Book> findByIdForUpdate(@Param("bookId") Long bookId);
}
