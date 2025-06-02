package com.database.booktrace.Repository;

import com.database.booktrace.Domain.Book;
import com.database.booktrace.Domain.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface BookRepository extends JpaRepository<Book,Long> {

    // Oracle용 카테고리별 추천 도서 조회
    @Query(value = """
    SELECT * FROM (
        SELECT b.*
        FROM BOOKS b
        WHERE b.category = ?1
        AND NOT EXISTS (
            SELECT 1 FROM LOANS l
            WHERE l.BOOK_ID = b.BOOK_ID
            AND l.USER_ID = ?2
            AND l.status = 'BORROWED'
        )
        ORDER BY b.AVAILABLE_AMOUNT DESC
    ) WHERE ROWNUM <= ?3
    """, nativeQuery = true)
    List<Book> findRecommendedBooksByCategory(
            @Param("category") String category,
            @Param("userId") Long userId,
            @Param("limit") int limit
    );

//    //대출 가능한 인기 도서 조회 (대출 횟수 기준)
//    @Query(value="""
//        SELECT *
//        FROM BOOKS b
//        LEFT JOIN loans l ON b.BOOK_ID=l.BOOK_ID
//        WHERE b.AVAILABLE_AMOUNT >0
//        GROUP BY b.BOOK_ID,b.LIBRARY_ID, b.TITLE, b.AUTHOR, b.PUBLISHER, b.CATEGORY,
//        b.PUBLISHED_DATE, b.DESCRIPTION, b.AVAILABLE_AMOUNT,b.COVER_IMAGE
//        ORDER BY COUNT(l.LOG_ID) DESC, b.AVAILABLE_COUNT DESC
//         WHERE ROWNUM <= :limit
//        """,nativeQuery = true)
//    List<Book> findPopularBooks(@Param("limit") int limit);
// Oracle용 인기 도서 조회
@Query(value = """
        SELECT * FROM (
            SELECT * FROM BOOKS
            ORDER BY AVAILABLE_AMOUNT DESC, BOOK_ID ASC
        ) WHERE ROWNUM <= ?1
        """, nativeQuery = true)
List<Book> findPopularBooks(int limit);

}

