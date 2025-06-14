package com.database.booktrace.Repository;

import com.database.booktrace.Domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository  {

    // Oracle용 카테고리별 추천 도서 조회 (subquery, Oracle 함수 사용)
    @Query(value = """
    SELECT * FROM (
        SELECT b.*
        FROM BOOKS b
        WHERE b.CATEGORY = ?1
        AND NOT EXISTS (
            SELECT 1 FROM LOANS l
            WHERE l.BOOK_ID = b.BOOK_ID
            AND l.USER_ID = ?2
            AND l.STATUS = 'BORROWED'
        )
        ORDER BY NVL(b.AVAILABLE_AMOUNT, 0) DESC
    ) WHERE ROWNUM <= ?3
    """, nativeQuery = true)
    List<Book> findRecommendedBooksByCategory(
            @Param("category") String category,
            @Param("userId") Long userId,
            @Param("limit") int limit
    );

    // Oracle용 인기 도서 조회 (Oracle 함수 사용)
    @Query(value = """
            SELECT * FROM (
                SELECT * FROM BOOKS
                WHERE NVL(AVAILABLE_AMOUNT, 0) >= 0
                ORDER BY NVL(AVAILABLE_AMOUNT, 0) DESC, BOOK_ID ASC
            ) WHERE ROWNUM <= ?1
            """, nativeQuery = true)
    List<Book> findPopularBooks(int limit);

    // 도서관별 도서 조회 (네이티브 SQL만 사용)
    @Query(value = "SELECT * FROM BOOKS WHERE LIBRARY_ID = ?1 ORDER BY TITLE", nativeQuery = true)
    List<Book> findByLibraryId(@Param("libraryId") Long libraryId);

    // 도서 ID와 도서관 ID로 도서 조회 (대출용, 네이티브 SQL)
    @Query(value = "SELECT * FROM BOOKS WHERE BOOK_ID = ?1 AND LIBRARY_ID = ?2", nativeQuery = true)
    Book findByBookIdAndLibraryId(@Param("bookId") Long bookId, @Param("libraryId") Long libraryId);

    // 대출 가능한 도서 조회 (Oracle NVL 함수 사용)
    @Query(value = """
        SELECT * FROM BOOKS
        WHERE NVL(AVAILABLE_AMOUNT, 0) > 0
        ORDER BY TITLE
        """, nativeQuery = true)
    List<Book> findAvailableBooks();

    // 제목 검색 (Oracle UPPER 함수 사용)
    @Query(value = """
        SELECT * FROM BOOKS 
        WHERE UPPER(TITLE) LIKE UPPER('%' || ?1 || '%')
        ORDER BY TITLE
        """, nativeQuery = true)
    List<Book> findByTitleContaining(String title);
}